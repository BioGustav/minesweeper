module dev.biogustav.minesweeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens dev.biogustav.minesweeper to javafx.fxml;
    exports dev.biogustav.minesweeper;
}