#pragma once

#include "gta4_phone_cheat_catalog.h"
#include "gta4_phone_quicksave_profiles.h"

namespace gta4::phone_cheats {
// Only used AFTER the decoded SCO hash and the Quicksave profile have matched.
// Resolve stock helper calls structurally instead of copying PC script offsets.
// An absent or ambiguous contract rejects the entire temporary patch.
namespace detail {
using namespace gta4::quicksave::bytecode;
inline std::vector<uint8_t> InstructionStarts(std::span<const uint8_t> code) {
  // Instruction widths from generated sub_82843700 (gta4_recomp.65.cpp).
  // Linked native pointers and inline strings must never be searched as opcodes.
  std::vector<uint8_t> starts(code.size());
  for (size_t at = 0; at < code.size();) {
    starts[at] = 1;
    const auto op = code[at];
    size_t width = 1;
    if (op == 34 || op == 35 || op == 36 || op == 41 || op == 42 || op == 46) width = 5;
    if (op == 40 || op == 48) width = 3;
    if (op == 45) width = 7;
    if (op == 47) width = 4;
    if (op >= 69 && op <= 72) width = 2;
    if (op == 66 || op == 67) {
      if (code.size() - at < 2) throw std::runtime_error("Truncated phone instruction");
      width = 2 + size_t(code[at + 1]) * (op == 66 ? 8 : 1);
    }
    if (width > code.size() - at) throw std::runtime_error("Truncated phone operand");
    at += width;
  }
  return starts;
}
inline size_t Unique(std::span<const uint8_t> code, const std::vector<uint8_t>& needle,
                     const std::vector<uint8_t>& starts) {
  size_t found = code.size();
  for (size_t at = 0; at + needle.size() <= code.size(); ++at) {
    if (!starts[at] || !std::equal(needle.begin(), needle.end(), code.begin() + at)) continue;
    if (found != code.size()) throw std::runtime_error("Ambiguous phone cheats contract");
    found = at;
  }
  if (found == code.size()) throw std::runtime_error("Missing phone cheats contract");
  return found;
}
inline bool Helper(std::span<const uint8_t> code, size_t at, uint8_t args) {
  if (at + 5 > code.size() || code[at] != kCall) return false;
  const auto target = Read32(code, at + 1);
  return target + uint64_t(4) <= code.size() && code[target] == 47 && code[target + 1] == args;
}
inline void Append(std::vector<uint8_t>& v, const gta4::quicksave::PhoneProfile& p,
                   uint32_t action, std::string_view key) {
  Local(v, p.menu_local); Local(v, p.options_local); Int(v, int32_t(action));
  String(v, key); String(v, ""); Int(v, 1); Int(v, 0); Int(v, 0);
  if (p.append_args == 10) { Int(v, 0); Int(v, 0); }
  Branch(v, kCall, p.append_function);
}
}  // namespace detail

inline gta4::quicksave::PhonePatch BuildCheatsPatch(
    std::span<const uint8_t> linked, const gta4::quicksave::PhoneProfile& p,
    gta4::quicksave::PhonePatch out, uint32_t request_thunk, uint32_t activate_thunk) {
  using namespace detail;
  // The existing bridge intentionally supports Niko's base game only.
  if (p.episode != 0 || !request_thunk || !activate_thunk || linked.size() != p.code_size)
    throw std::runtime_error("Unsupported phone cheats profile or native binding");
  if (out.code.size() < linked.size()) throw std::runtime_error("Missing base phone patch");
  const auto starts = InstructionStarts(linked);
  std::vector<uint8_t> stock_root;
  Append(stock_root, p, kMenuState, "CP_CHEATS");
  const auto root = Unique(linked, stock_root, starts);
  std::vector<uint8_t> marker;
  String(marker, "CP_CHEATS_");
  const auto label = Unique(linked, marker, starts);

  // Stock state 1035: reset controller (4 args), conditional unlocked-cheat
  // loop, then finalize/render the controller (6 args). Keep both helpers.
  size_t reset = linked.size(), finish = linked.size();
  for (size_t at = label > 256 ? label - 256 : 0; at < label; ++at)
    if (starts[at] && Helper(linked, at, 4)) reset = at;
  for (size_t at = label + marker.size(); at < std::min(linked.size(), label + 256); ++at)
    if (starts[at] && Helper(linked, at, 6)) {
      if (finish != linked.size()) throw std::runtime_error("Ambiguous cheats renderer");
      finish = at;
    }
  if (reset == linked.size() || finish == linked.size() || reset + 10 >= label)
    throw std::runtime_error("Missing cheats menu helpers");
  // Recover the complete renderer argument prefix. The view-array local is
  // profile-specific; enumerate only declared locals, requiring one match.
  size_t render = linked.size();
  for (uint32_t local = 0; local < p.local_count; ++local) {
    std::vector<uint8_t> args;
    Local(args, p.menu_local); Local(args, p.options_local); Local(args, local);
    Int(args, 1); Int(args, 1); Int(args, 0);
    if (finish >= args.size() && std::equal(args.begin(), args.end(), linked.begin() + finish - args.size())) {
      if (render != linked.size()) throw std::runtime_error("Ambiguous cheats renderer arguments");
      render = finish - args.size();
    }
  }
  if (render <= label || render == linked.size())
    throw std::runtime_error("Missing cheats renderer arguments");

  // The stock accept handler takes the selected option, dispatches the retail
  // cheat mapping and returns an ignored boolean. Identify it by its linked
  // ACTIVATE_CHEAT calls, never by a PC offset or an unvalidated menu action.
  std::vector<uint8_t> selected, activate;
  Local(selected, p.menu_local);
  selected.insert(selected.end(), {kPushS, 60, 0});
  selected.push_back(kAdd); selected.push_back(kRefGet);
  Native(activate, activate_thunk, 1, 0);
  size_t accept = linked.size();
  for (size_t at = 0; at + selected.size() + 6 <= linked.size(); ++at) {
    if (!starts[at] || !std::equal(selected.begin(), selected.end(), linked.begin() + at)) continue;
    const auto call = at + selected.size();
    if (!Helper(linked, call, 1) || linked[call + 5] != 44) continue;
    const auto target = Read32(linked, call + 1);
    size_t calls = 0;
    for (size_t pos = target + 4; pos + activate.size() <= linked.size(); ++pos) {
      if (!starts[pos]) continue;
      if (linked[pos] == 47) break;
      if (std::equal(activate.begin(), activate.end(), linked.begin() + pos)) ++calls;
    }
    if (calls < 15) continue;
    if (accept != linked.size()) throw std::runtime_error("Ambiguous cheats accept handler");
    accept = at;
  }
  if (accept == linked.size()) throw std::runtime_error("Missing cheats accept handler");

  auto& v = out.code;
  const auto menu = uint32_t(v.size());
  for (size_t i = 0; i < kCatalog.size(); ++i)
    Append(v, p, kActionBase + uint32_t(i), kLabelKeys[i]);
  Branch(v, kJump, uint32_t(render));
  v[reset + 5] = kJump; Write32(v, reset + 6, menu);

  const auto dispatch = uint32_t(v.size());
  Int(v, 0); Native(v, request_thunk, 1, 1);
  const auto denied = Branch(v, kJumpFalse, 0);
  Int(v, int32_t(gta4::quicksave::kClosingPhoneState));
  Branch(v, kCall, p.set_state_function);
  Write32(v, denied + 1, uint32_t(v.size()));
  Branch(v, kJump, uint32_t(accept + selected.size() + 6));
  v[accept] = kJump; Write32(v, accept + 1, dispatch);

  // Always expose exactly one stock Cheats destination, without changing the
  // persistent unlocked-cheat bitset or executing any cheat during menu build.
  v[root] = kJump; Write32(v, root + 1, uint32_t(root + stock_root.size()));
  const auto entry = uint32_t(v.size());
  Append(v, p, kMenuState, "LR_CHEAT");
  Branch(v, kJump, out.append_extension);
  v[p.append_site] = kJump; Write32(v, p.append_site + 1, entry);
  return out;
}
}  // namespace gta4::phone_cheats
