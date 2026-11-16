#pragma once

#include <array>
#include <cstdint>
#include <string_view>

// Menu data only. This catalogue never invokes or queues a cheat.
// The phone script integration is a separate, explicitly tested step.
namespace gta4::phone_cheats {

enum class Category : std::uint8_t {
  kJugador, kArmas, kPolicia, kVehiculos, kMundo
};

struct Entry {
  std::uint32_t index;
  std::string_view label;
  Category category;
};

// All GTA IV base-game cheats supported by the existing native bridge,
// plus the ten local offline actions. Keep these IDs aligned with
// gta4_cheat_policy.h and CheatsUi.java.
inline constexpr std::array<Entry, 25> kCatalog{{
    {2, "Salud, chaleco y municiones", Category::kJugador},
    {0, "Armas - paquete 1", Category::kArmas},
    {1, "Armas - paquete 2", Category::kArmas},
    {4, "Quitar estrellas de policia", Category::kPolicia},
    {3, "Aumentar busqueda policial", Category::kPolicia},
    {6, "Cambiar clima", Category::kMundo},
    {7, "Generar helicoptero Annihilator", Category::kVehiculos},
    {8, "Generar moto NRG 900", Category::kVehiculos},
    {9, "Generar FIB Buffalo", Category::kVehiculos},
    {10, "Generar lancha Jetmax", Category::kVehiculos},
    {11, "Generar Comet", Category::kVehiculos},
    {12, "Generar Turismo", Category::kVehiculos},
    {13, "Generar Cognoscenti", Category::kVehiculos},
    {14, "Generar Super GT", Category::kVehiculos},
    {15, "Generar moto Sanchez", Category::kVehiculos},
    {100, "Restaurar salud", Category::kJugador},
    {101, "Anadir chaleco antibalas", Category::kJugador},
    {102, "Reparar vehiculo actual", Category::kVehiculos},
    {103, "Anadir $10.000", Category::kJugador},
    {104, "Activar inmortalidad", Category::kJugador},
    {105, "Desactivar inmortalidad", Category::kJugador},
    {106, "Cambiar a la manana", Category::kMundo},
    {107, "Cambiar al mediodia", Category::kMundo},
    {108, "Cambiar al atardecer", Category::kMundo},
    {109, "Cambiar a medianoche", Category::kMundo},
}};

constexpr const Entry* Find(std::uint32_t index) noexcept {
  for (const Entry& entry : kCatalog)
    if (entry.index == index)
      return &entry;
  return nullptr;
}

constexpr bool ValidCatalog() noexcept {
  if (kCatalog.size() != 25)
    return false;
  for (std::size_t i = 0; i < kCatalog.size(); ++i) {
    const Entry& entry = kCatalog[i];
    if (entry.label.empty() ||
        !((entry.index < 16 && entry.index != 5) ||
          (entry.index >= 100 && entry.index <= 109)))
      return false;
    for (std::size_t j = i + 1; j < kCatalog.size(); ++j)
      if (entry.index == kCatalog[j].index)
        return false;
  }
  return true;
}

static_assert(ValidCatalog(), "Phone cheat menu must have 25 unique, supported entries");
}  // namespace gta4::phone_cheats
