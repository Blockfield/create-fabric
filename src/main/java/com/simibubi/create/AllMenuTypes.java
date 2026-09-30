package com.simibubi.create;

import com.simibubi.create.content.equipment.blueprint.BlueprintMenu;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxScreen;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import com.simibubi.create.content.logistics.filter.PackageFilterMenu;
import com.simibubi.create.content.logistics.filter.PackageFilterScreen;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerMenu;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.content.schematics.cannon.SchematicannonScreen;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;
import com.simibubi.create.content.schematics.table.SchematicTableScreen;
import com.simibubi.create.content.trains.schedule.ScheduleMenu;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.fabric.MenuUtil;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class AllMenuTypes {

    public static final RegistryEntry<MenuType<?>, MenuType<SchematicTableMenu>> SCHEMATIC_TABLE =
            register("schematic_table", SchematicTableMenu::new, () -> SchematicTableScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<SchematicannonMenu>> SCHEMATICANNON =
            register("schematicannon", SchematicannonMenu::new, () -> SchematicannonScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<FilterMenu>> FILTER =
            register("filter", FilterMenu::new, () -> FilterScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<AttributeFilterMenu>> ATTRIBUTE_FILTER =
            register(
                    "attribute_filter", AttributeFilterMenu::new, () -> AttributeFilterScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<PackageFilterMenu>> PACKAGE_FILTER =
            register("package_filter", PackageFilterMenu::new, () -> PackageFilterScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<BlueprintMenu>> CRAFTING_BLUEPRINT =
            register("crafting_blueprint", BlueprintMenu::new, () -> BlueprintScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<LinkedControllerMenu>>
            LINKED_CONTROLLER =
                    register(
                            "linked_controller",
                            LinkedControllerMenu::new,
                            () -> LinkedControllerScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<ToolboxMenu>> TOOLBOX =
            register("toolbox", ToolboxMenu::new, () -> ToolboxScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<ScheduleMenu>> SCHEDULE =
            register("schedule", ScheduleMenu::new, () -> ScheduleScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<StockKeeperCategoryMenu>>
            STOCK_KEEPER_CATEGORY =
                    register(
                            "stock_keeper_category",
                            StockKeeperCategoryMenu::new,
                            () -> StockKeeperCategoryScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<StockKeeperRequestMenu>>
            STOCK_KEEPER_REQUEST =
                    register(
                            "stock_keeper_request",
                            StockKeeperRequestMenu::new,
                            () -> StockKeeperRequestScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<PackagePortMenu>> PACKAGE_PORT =
            register("package_port", PackagePortMenu::new, () -> PackagePortScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<RedstoneRequesterMenu>>
            REDSTONE_REQUESTER =
                    register(
                            "redstone_requester",
                            RedstoneRequesterMenu::new,
                            () -> RedstoneRequesterScreen::new);

    public static final RegistryEntry<MenuType<?>, MenuType<FactoryPanelSetItemMenu>>
            FACTORY_PANEL_SET_ITEM =
                    register(
                            "factory_panel_set_item",
                            FactoryPanelSetItemMenu::new,
                            () -> FactoryPanelSetItemScreen::new);

    // fabric: Registrate's ForgeMenuFactory hands the extra data over as a raw Object; menus
    // declare it
    // as RegistryFriendlyByteBuf, so the cast happens here instead of at every call site.
    public interface ExtraDataMenuFactory<C extends AbstractContainerMenu> {
        C create(MenuType<C> type, int windowId, Inventory inv, RegistryFriendlyByteBuf extraData);
    }

    // fabric: Registrate builds ExtendedScreenHandlerType(factory, null), and a null packet codec
    // NPEs the
    // moment a menu carrying extra data is opened. Register the type here so it gets
    // MenuUtil.BUFFER_CODEC.
    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
            RegistryEntry<MenuType<?>, MenuType<C>> register(
                    String name,
                    ExtraDataMenuFactory<C> factory,
                    NonNullSupplier<ScreenFactory<C, S>> screenFactory) {
        @SuppressWarnings("unchecked")
        MenuType<C>[] self = new MenuType[1];
        NonNullSupplier<MenuType<C>> supplier =
                () -> {
                    ExtendedScreenHandlerType<C, RegistryFriendlyByteBuf> type =
                            new ExtendedScreenHandlerType<>(
                                    (windowId, inv, data) ->
                                            factory.create(self[0], windowId, inv, data),
                                    MenuUtil.BUFFER_CODEC);
                    self[0] = type;
                    CatnipServices.PLATFORM.executeOnClientOnly(
                            () -> () -> registerScreen(type, screenFactory));
                    return type;
                };
        return Create.registrate().simple(name, Registries.MENU, supplier);
    }

    @Environment(EnvType.CLIENT)
    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
            void registerScreen(
                    MenuType<C> type, NonNullSupplier<ScreenFactory<C, S>> screenFactory) {
        ScreenFactory<C, S> screens = screenFactory.get();
        MenuScreens.register(type, screens::create);
    }

    public static void register() {}
}
