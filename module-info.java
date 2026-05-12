module com.hediye {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.hediye to javafx.fxml;
    opens com.hediye.ui to javafx.fxml;
    opens com.hediye.model to javafx.base;

    exports com.hediye;
    exports com.hediye.model;
    exports com.hediye.datastructures;
    exports com.hediye.database;
    exports com.hediye.service;
    exports com.hediye.ui;
}
