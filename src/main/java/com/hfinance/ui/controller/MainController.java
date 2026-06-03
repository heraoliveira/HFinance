package com.hfinance.ui.controller;

import com.hfinance.core.config.HFinanceContext;
import com.hfinance.ui.component.SidebarMenu;
import com.hfinance.ui.navigation.Router;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MainController {
    private final BorderPane root = new BorderPane();
    private final Label title = new Label("Visão geral");

    public MainController(HFinanceContext context) {
        root.getStyleClass().add("app-root");
        title.getStyleClass().add("screen-title");

        HBox topbar = new HBox(title);
        topbar.getStyleClass().add("topbar");
        root.setTop(topbar);

        Router router = new Router(root, title);
        SidebarMenu sidebarMenu = new SidebarMenu(router::navigate);
        root.setLeft(sidebarMenu);

        router.register("Visão geral", () -> content(new DashboardController(context).getView()));
        router.register("Contas", () -> content(new AccountController(context).getView()));
        router.register("Transações", () -> content(new TransactionController(context).getView()));
        router.register("Orçamentos", () -> content(new BudgetController(context).getView()));
        router.register("Metas", () -> content(new GoalController(context).getView()));
        router.register("Relatórios", () -> content(new ReportController(context).getView()));
        router.register("Sobre", () -> content(new AboutController(context).getView()));

        sidebarMenu.activate("Visão geral");
        router.navigate("Visão geral");
    }

    public Parent getView() {
        return root;
    }

    private StackPane content(Parent parent) {
        StackPane wrapper = new StackPane(parent);
        wrapper.getStyleClass().add("content");
        return wrapper;
    }
}
