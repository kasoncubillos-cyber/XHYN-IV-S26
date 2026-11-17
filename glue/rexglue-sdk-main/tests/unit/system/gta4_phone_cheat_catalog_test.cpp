#include <array>
#include <iostream>
#include <stdexcept>
#include "../../../gta4-recomp/src/gta4_phone_cheat_catalog.h"

#ifdef GTA4_PHONE_CHEATS_STANDALONE
static std::size_t checks = 0;
#define CHECK_PHONE(...) do { ++checks; if (!(__VA_ARGS__)) throw std::runtime_error(#__VA_ARGS__); } while(false)
#else
#include <catch2/catch_test_macros.hpp>
#define CHECK_PHONE(...) REQUIRE(__VA_ARGS__)
#endif

namespace {
void VerifyPhoneCheatCatalog() {
  using namespace gta4::phone_cheats;
  constexpr std::array<std::uint32_t, 25> expected{
      2, 0, 1, 4, 3, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
      100, 101, 102, 103, 104, 105, 106, 107, 108, 109};
  CHECK_PHONE(ValidCatalog());
  CHECK_PHONE(kCatalog.size() == expected.size());
  for (std::size_t i = 0; i < expected.size(); ++i) {
    CHECK_PHONE(kCatalog[i].index == expected[i]);
    CHECK_PHONE(Find(expected[i]) == &kCatalog[i]);
    CHECK_PHONE(!kCatalog[i].label.empty());
  }
  CHECK_PHONE(Find(5) == nullptr);   // Unsupported stock money placeholder.
  CHECK_PHONE(Find(16) == nullptr);
  CHECK_PHONE(Find(110) == nullptr);
  // Reading catalogue data must not activate a native cheat.
}
}

#ifdef GTA4_PHONE_CHEATS_STANDALONE
int main() {
  try { VerifyPhoneCheatCatalog();
        std::cout << "PASS " << checks << " phone cheat catalogue checks\n";
  } catch (const std::exception& error) {
    std::cerr << error.what() << '\n';
    return 1;
  }
}
#else
TEST_CASE("Phone cheat catalogue has complete unique identifiers", "[gta4-phone-cheats]") {
  VerifyPhoneCheatCatalog();
}
#endif
