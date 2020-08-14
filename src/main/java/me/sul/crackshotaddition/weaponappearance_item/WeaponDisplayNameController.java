package me.sul.crackshotaddition.weaponappearance_item;

import com.shampaggon.crackshot.events.WeaponReloadCompleteEvent;
import com.shampaggon.crackshot.events.WeaponReloadEvent;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import me.sul.crackshotaddition.MainCrackShotWeaponInfoMetaManager;
import me.sul.crackshotaddition.events.WeaponSwapCompleteEvent;
import me.sul.crackshotaddition.events.WeaponSwapEvent;
import me.sul.servercore.inventoryevent.InventoryItemChangedEvent;
import me.sul.servercore.inventoryevent.PlayerMainItemChangedConsideringUidEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeaponDisplayNameController implements Listener {
    private static final String AMMO_ICON1 = "§f锄 "; // §f 없으면 색 이상해짐
    private static final int MIDDLE_BLANK_LENGTH = 7;

    enum DisplayNameType {
        NORMAL,
        RELOAD,
        SWAPPING
    }

    // NORMAL
    @EventHandler(priority = EventPriority.NORMAL) // onSwap보다 선행돼야 함 (Swap에게 덮어 씌워져야하기 때문)
    public void onPlayerMainItemChanged(PlayerMainItemChangedConsideringUidEvent e) {
        if (e.isChangedToCrackShotWeapon()) {
            updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.NORMAL);
        }
    }

    @EventHandler
    public void onShoot(WeaponShootEvent e) {
        updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.NORMAL);
    }

    @EventHandler
    public void onReloadComplete(WeaponReloadCompleteEvent e) {
        updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.NORMAL);
    }

    @EventHandler
    public void onItemChanged(InventoryItemChangedEvent e) {
        Player p = e.getPlayer();
        Material requiredAmmoMaterial = MainCrackShotWeaponInfoMetaManager.getAmmoItemMaterial(p);
        if (requiredAmmoMaterial != null && (e.getItemStack().getType() == requiredAmmoMaterial || e.getItemStack().getType() == Material.AIR)) {
            updateMainWeaponDisplay(p, DisplayNameType.NORMAL);
        }
    }


    // RELOADING
    @EventHandler(priority = EventPriority.HIGH)
    public void onReload(WeaponReloadEvent e) {
        if (e.isCancelled()) return;
        updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.RELOAD);
    }


    // SWAPPING
    @EventHandler(priority = EventPriority.HIGH) // onPlayerMainItemChanged보다 후행돼야 함 (덮어 씌워야하기 때문)
    public void onSwap(WeaponSwapEvent e) {
        updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.SWAPPING);
    }

    @EventHandler
    public void onSwapComplete(WeaponSwapCompleteEvent e) {
        updateMainWeaponDisplay(e.getPlayer(), DisplayNameType.NORMAL);
    }



    private void updateMainWeaponDisplay(Player p, DisplayNameType displayNameType) {
        ItemStack weapon = null;
        String configName = null;
        Integer leftAmmo = null;
        Integer rightAmmo = null;
        Integer possessedExtraAmmoAmt = null;

        weapon = MainCrackShotWeaponInfoMetaManager.getItemStack(p);
        configName = MainCrackShotWeaponInfoMetaManager.getConfigName(p);
        if (displayNameType == DisplayNameType.NORMAL) {
            leftAmmo = MainCrackShotWeaponInfoMetaManager.getLeftAmmoAmount(p);
            rightAmmo = MainCrackShotWeaponInfoMetaManager.getRightAmmoAmount(p);
            possessedExtraAmmoAmt = MainCrackShotWeaponInfoMetaManager.getPossessedExtraAmmoAmount(p);
        }
        makePrettyWeaponDisplayName(p, displayNameType, weapon, configName, leftAmmo, rightAmmo, possessedExtraAmmoAmt);
    }


    // 외부에서 사용할 때는 무조건 DisplayNameType.NORMAL
    public static void makePrettyWeaponDisplayName(@Nullable Player p, @Nonnull ItemStack weapon, @Nonnull String configName, @Nullable Integer leftAmmo, @Nullable Integer rightAmmo, @Nullable Integer possessedExtraAmmoAmt) {
        makePrettyWeaponDisplayName(p, DisplayNameType.NORMAL, weapon, configName, leftAmmo, rightAmmo, possessedExtraAmmoAmt);
    }
	private static void makePrettyWeaponDisplayName(@Nullable Player p, @Nonnull DisplayNameType displayNameType, @Nonnull ItemStack weapon, @Nonnull String configName, @Nullable Integer leftAmmo, @Nullable Integer rightAmmo, @Nullable Integer possessedExtraAmmoAmt) {
        boolean hasRightAmmo = (rightAmmo != null && rightAmmo >= 0);

        ItemMeta meta = weapon.getItemMeta();
        StringBuilder weaponNameBuilder = new StringBuilder();

        weaponNameBuilder.append(configName);  // 총기 이름 넣기

        for (int i = 0; i < MIDDLE_BLANK_LENGTH; i++) {
            weaponNameBuilder.append(" ");  // 중간 공백 넣기
        }
        if (displayNameType == DisplayNameType.NORMAL) {
            if (possessedExtraAmmoAmt == null) possessedExtraAmmoAmt = 0;
            if (leftAmmo == null) leftAmmo = 0;

            // 왼쪽 총알 넣기
            if (leftAmmo == 0) {
                weaponNameBuilder.append(AMMO_ICON1 + "§c").append(leftAmmo);
            } else {
                weaponNameBuilder.append(AMMO_ICON1 + "§f").append(leftAmmo);
            }

            // | 및 오른쪽 총알 넣기
            if (hasRightAmmo) {
                if (rightAmmo == 0) {
                    weaponNameBuilder.append(" §f| §c").append(rightAmmo).append(" ");
                } else {
                    weaponNameBuilder.append(" §f| ").append(rightAmmo).append(" ");
                }
            }

            // 슬래쉬 및 보유 총알 넣기
            if (possessedExtraAmmoAmt == 0) {
                weaponNameBuilder.append("§7/§4").append(possessedExtraAmmoAmt);
            } else {
                weaponNameBuilder.append("§7/").append(possessedExtraAmmoAmt);
            }

            meta.setDisplayName(weaponNameBuilder.toString());
        }
        else if (displayNameType == DisplayNameType.RELOAD) {
            weaponNameBuilder.append(AMMO_ICON1 + "§cRELOADING..");

            meta.setDisplayName(weaponNameBuilder.toString());
        }
        else if (displayNameType == DisplayNameType.SWAPPING) {
            weaponNameBuilder.append(AMMO_ICON1 + "§cSWAPPING..");

            meta.setDisplayName(weaponNameBuilder.toString());
        }

        weapon.setItemMeta(meta);
        if (p != null) {
            p.updateInventory();
        }
    }
}