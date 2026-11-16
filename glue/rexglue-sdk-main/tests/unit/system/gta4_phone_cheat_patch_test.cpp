#include <iostream>
#include <stdexcept>
#include "../../../gta4-recomp/src/gta4_phone_cheat_patch.h"
#include "../../../gta4-recomp/src/gta4_cheat_policy.h"
#ifdef GTA4_PHONE_CHEATS_STANDALONE
#define CHECK_P(...) do { if (!(__VA_ARGS__)) throw std::runtime_error(#__VA_ARGS__); } while(false)
#else
#include <catch2/catch_test_macros.hpp>
#define CHECK_P(...) REQUIRE((__VA_ARGS__))
#endif
namespace {
using namespace gta4::quicksave;
using namespace gta4::phone_cheats;
using namespace gta4::quicksave::bytecode;
constexpr uint32_t kActivate = 0x90000030, kRequest = 0x90000020;
// Synthetic linked instruction fixture: verifies the adapter's contracts and
// emitted paths. It is deliberately NOT represented as an installed game SCO.
struct Fixture {
  PhoneProfile p = kPhoneProfiles[0];
  std::vector<uint8_t> code = std::vector<uint8_t>(p.code_size);
  size_t reset = 0x3000, render = 0, accept = 0x4000, stock = 0x2800;
  explicit Fixture(bool stock_offset = false) {
    std::vector<uint8_t> prefix;
    Local(prefix, p.menu_local); Local(prefix, p.options_local);
    std::copy_n(prefix.begin(), 5, code.begin() + p.append_site);
    prefix.clear(); Local(prefix, p.menu_local); prefix.insert(prefix.end(), {kPushS, 60, 0});
    std::copy(prefix.begin(), prefix.end(), code.begin() + p.accept_site);
    code[p.append_function] = 47; code[p.append_function + 1] = p.append_args;
    code[p.set_state_function] = 47; code[p.set_state_function + 1] = 1;
    code[p.accept_exit] = kJump;
    code[0x2000] = 47; code[0x2001] = 4;
    code[0x2100] = 47; code[0x2101] = 6;
    code[0x5000] = 47; code[0x5001] = 1;
    prefix.clear(); detail::Append(prefix, p, kMenuState, "CP_CHEATS");
    std::copy(prefix.begin(), prefix.end(), code.begin() + stock);
    prefix.clear(); Branch(prefix, kCall, 0x2000);
    prefix.insert(prefix.end(), 16, 0);
    String(prefix, "CP_CHEATS_");
    // The actual loop calls the option helper before finalizing.
    Branch(prefix, kCall, p.append_function);
    render = reset + prefix.size();
    Local(prefix, p.menu_local); Local(prefix, p.options_local);
    if (stock_offset) prefix.insert(prefix.end(), {kPushS, 38, 0, kLocal});
    else Local(prefix, 38);
    Int(prefix, 1); Int(prefix, 1); Int(prefix, 0); Branch(prefix, kCall, 0x2100);
    std::copy(prefix.begin(), prefix.end(), code.begin() + reset);
    prefix.clear(); Local(prefix, p.menu_local); prefix.insert(prefix.end(), {kPushS, 60, 0});
    prefix.push_back(kAdd); prefix.push_back(kRefGet); Branch(prefix, kCall, 0x5000);
    prefix.push_back(44);
    std::copy(prefix.begin(), prefix.end(), code.begin() + accept);
    prefix.clear();
    for (unsigned i = 0; i < 16; ++i) { Int(prefix, int(i)); Native(prefix, kActivate, 1, 0); }
    std::copy(prefix.begin(), prefix.end(), code.begin() + 0x5004);
  }
  PhonePatch Patch() const {
    return BuildCheatsPatch(code, p, BuildPhonePatch(code, p, 0x90000000, 0x90000010), kRequest, kActivate);
  }
};
void Verify() {
  Fixture retail(true); CHECK_P(retail.Patch().code[retail.reset + 5] == kJump);
  Fixture f;
  auto out = f.Patch();
  CHECK_P(out.code[f.reset + 5] == kJump);
  auto pc = Read32(out.code, f.reset + 6);
  // The entire generated menu must contain only 25 calls to the existing
  // append helper, followed by a jump back to the stock renderer.
  for (size_t i = 0; i < kCatalog.size(); ++i) {
    std::vector<uint8_t> expected;
    detail::Append(expected, f.p, kActionBase + uint32_t(i), kLabelKeys[i]);
    CHECK_P(std::equal(expected.begin(), expected.end(), out.code.begin() + pc));
    CHECK_P(FromAction(kActionBase + uint32_t(i)) == &kCatalog[i]);
    pc += uint32_t(expected.size());
  }
  CHECK_P(out.code[pc] == kJump && Read32(out.code, pc + 1) == f.render);
  CHECK_P(!FromAction(kActionBase - 1) && !FromAction(kActionBase + 25));
  CHECK_P(out.code[f.stock] == kJump);
  CHECK_P(out.code[f.accept] == kJump);
  for (unsigned failure = 0; failure < 6; ++failure) {
    Fixture broken;
    if (failure == 0) broken.code[broken.stock] = 0;
    if (failure == 1) broken.code[broken.reset] = 0;
    if (failure == 2) broken.code[broken.render] = 0;
    if (failure == 3) broken.code[broken.accept] = 0;
    if (failure == 4) broken.p.episode = 1;
    if (failure == 5) {
      std::vector<uint8_t> duplicate; detail::Append(duplicate, broken.p, kMenuState, "CP_CHEATS");
      std::copy(duplicate.begin(), duplicate.end(), broken.code.begin() + 0x6000);
    }
    bool rejected = false;
    try { broken.Patch(); } catch (const std::exception&) { rejected = true; }
    CHECK_P(rejected);
  }
  gta4::input::TouchContextSnapshot context{};
  context.valid = context.playing = context.gameplay_allowed = true;
  gta4::cheats::Request request{2, 1, 1000}; request.phone_origin = true;
  CHECK_P(gta4::cheats::Decide(request, context, 10, 1, false) == gta4::cheats::Decision::kWait);
  CHECK_P(gta4::cheats::Decide(request, context, 10, 1, true) == gta4::cheats::Decision::kExecute);
  CHECK_P(gta4::cheats::Decide(request, context, 1000, 1, false) == gta4::cheats::Decision::kCancel);
  CHECK_P(gta4::cheats::Decide(request, context, 10, 2, false) == gta4::cheats::Decision::kCancel);
}
}
#ifdef GTA4_PHONE_CHEATS_STANDALONE
int main() { try { Verify(); std::cout << "PASS phone cheats patch contracts and close-before-execute policy\n"; }
  catch (const std::exception& e) { std::cerr << e.what() << '\n'; return 1; } }
#else
TEST_CASE("Phone cheats use stock menu and guarded selection", "[gta4-phone-cheats]") { Verify(); }
#endif
