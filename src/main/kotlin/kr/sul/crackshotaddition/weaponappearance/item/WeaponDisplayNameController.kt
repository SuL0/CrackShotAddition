package kr.sul.crackshotaddition.weaponappearance.item

import com.shampaggon.crackshot.events.WeaponReloadCompleteEvent
import com.shampaggon.crackshot.events.WeaponReloadEvent
import com.shampaggon.crackshot.events.WeaponShootEvent
import kr.sul.crackshotaddition.events.WeaponSwapCompleteEvent
import kr.sul.crackshotaddition.events.WeaponSwapEvent
import kr.sul.crackshotaddition.infomanager.extractor.WeaponInfoExtractor
import kr.sul.crackshotaddition.infomanager.ammo.PlayerInvAmmoInfoManager
import kr.sul.servercore.inventoryevent.InventoryItemChangedEvent
import kr.sul.servercore.inventoryevent.PlayerHeldItemIsChangedToOnotherEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object WeaponDisplayNameController : Listener {
    enum class DisplayNameType {
        NORMAL, RELOADING, SWAPPING
    }
    private const val AMMO_ICON1 = "§f锄 " // §f 없으면 색 이상해짐
    private const val MIDDLE_BLANK_LENGTH = 7

    // NORMAL
    @EventHandler(priority = EventPriority.NORMAL) // onSwap보다 선행돼야 함 (Swap에게 덮어 씌워져야하기 때문)
    fun onPlayerHeldItemChanged(e: PlayerHeldItemIsChangedToOnotherEvent) {
        if (e.isChangedToCrackShotWeapon()) {
            updateHeldWeaponDisplay(e.player, DisplayNameType.NORMAL)
        }
    }

    @EventHandler
    fun onShoot(e: WeaponShootEvent) {
        updateHeldWeaponDisplay(e.player, DisplayNameType.NORMAL)
    }

    @EventHandler
    fun onReloadComplete(e: WeaponReloadCompleteEvent) {
        updateHeldWeaponDisplay(e.player, DisplayNameType.NORMAL)
    }

    @EventHandler
    fun onItemChanged(e: InventoryItemChangedEvent) {
        val p = e.player
        val heldItem = p.inventory.itemInMainHand
        if (!WeaponInfoExtractor.isValidCrackShotWeapon(heldItem)) return
        val ammoItem = WeaponInfoExtractor(p, heldItem).ammoNeeded?.itemStack

        // 바뀌게 된 아이템이 Ammo / AIR
        if (ammoItem != null && (e.newItemStack.type == ammoItem.type || e.newItemStack.type == Material.AIR)) {
            // Reload나 Swapping중이였다면, 이게 무시돼야 함
            val heldItemName = heldItem.itemMeta.displayName
            if (heldItemName.contains(DisplayNameType.RELOADING.name) || heldItemName.contains(DisplayNameType.SWAPPING.name)) return

            updateHeldWeaponDisplay(p, DisplayNameType.NORMAL)
        }
    }

    // RELOADING
    @EventHandler(priority = EventPriority.HIGH)
    fun onReload(e: WeaponReloadEvent) {
        if (e.isCancelled) return
        updateHeldWeaponDisplay(e.player, DisplayNameType.RELOADING)
    }

    // SWAPPING
    @EventHandler(priority = EventPriority.HIGH) // onPlayerHeldItemChanged보다 후행돼야 함 (덮어 씌워야하기 때문)
    fun onSwap(e: WeaponSwapEvent) {
        if (e.swapDelay > 0) {
            updateHeldWeaponDisplay(e.player, DisplayNameType.SWAPPING)
        }
    }

    @EventHandler
    fun onSwapComplete(e: WeaponSwapCompleteEvent) {
        updateHeldWeaponDisplay(e.player, DisplayNameType.NORMAL)
    }

    private fun updateHeldWeaponDisplay(p: Player, displayNameType: DisplayNameType) {
        updateWeaponDisplay(p, p.inventory.itemInMainHand, displayNameType)
    }
    fun updateWeaponDisplay(p: Player, item: ItemStack, displayNameType: DisplayNameType) {
        if (!WeaponInfoExtractor.isValidCrackShotWeapon(item)) throw Exception("$p, $item, $displayNameType")
        val weaponInfo = WeaponInfoExtractor(p, item)
        val weapon = weaponInfo.item
        val configName = weaponInfo.configName
        var leftAmmo: Int? = null
        var rightAmmo: Int? = null
        var reloadableAmt: Int? = null
        if (displayNameType == DisplayNameType.NORMAL) {
            leftAmmo = weaponInfo.leftAmmoAmt
            rightAmmo = weaponInfo.rightAmmoAmt

            val invAmmoInfo = PlayerInvAmmoInfoManager.getInfo(p)
            reloadableAmt = weaponInfo.ammoNeeded?.let { invAmmoInfo.getReloadableAmountPerWeapon(p, weapon)!! }
        }
        makePrettyWeaponDisplayName(p, displayNameType, weapon, configName, leftAmmo, rightAmmo, reloadableAmt)
    }


    @JvmStatic  // 외부에서 사용할 때는 무조건 DisplayNameType.NORMAL
    fun makePrettyWeaponDisplayName(p: Player?, item: ItemStack, configName: String, leftAmmo: Int?, rightAmmo: Int?, reloadableAmount: Int?) {
        makePrettyWeaponDisplayName(p, DisplayNameType.NORMAL, item, configName, leftAmmo, rightAmmo, reloadableAmount)
    }
    private fun makePrettyWeaponDisplayName(p: Player?, displayNameType: DisplayNameType, item: ItemStack, configName: String, leftAmmo: Int?, rightAmmo: Int?, reloadableAmt: Int?) {
        val meta = item.itemMeta
        val weaponNameBuilder = StringBuilder()
        weaponNameBuilder.append(configName) // 총기 이름 넣기
        for (i in 0 until MIDDLE_BLANK_LENGTH) {
            weaponNameBuilder.append(" ") // 중간 공백 넣기
        }
        if (displayNameType == DisplayNameType.NORMAL) {
            if (leftAmmo != Integer.MAX_VALUE) {
                // 왼쪽 총알 넣기
                if (leftAmmo == 0) {
                    weaponNameBuilder.append("${AMMO_ICON1}§c").append(leftAmmo)
                } else {
                    weaponNameBuilder.append("${AMMO_ICON1}§f").append(leftAmmo)
                }

                // | 및 오른쪽 총알 넣기
                if (rightAmmo != null) {
                    if (rightAmmo == 0) {
                        weaponNameBuilder.append(" §f| §c").append(rightAmmo).append(" ")
                    } else {
                        weaponNameBuilder.append(" §f| ").append(rightAmmo).append(" ")
                    }
                }

                // 슬래쉬 및 보유 총알 넣기
                if (reloadableAmt == null || reloadableAmt == 0) {
                    weaponNameBuilder.append("§7/§4").append(reloadableAmt ?: 0)
                } else {
                    weaponNameBuilder.append("§7/").append(reloadableAmt)
                }
            }
            // 무한
            else {
                weaponNameBuilder.append("§dInfinity")
            }
        } else if (displayNameType == DisplayNameType.RELOADING) {
            weaponNameBuilder.append("${AMMO_ICON1}§cRELOADING..")
        } else if (displayNameType == DisplayNameType.SWAPPING) {
            weaponNameBuilder.append("${AMMO_ICON1}§cSWAPPING..")
        }
        meta.displayName = weaponNameBuilder.toString()

        item.itemMeta = meta
        p?.updateInventory()
    }
}