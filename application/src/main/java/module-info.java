module com.bitbybit {
	requires javafx.fxml;
	requires javafx.controls;
	// requires javafx.graphics;
	requires transitive javafx.graphics;
	requires com.jfoenix;
	requires MaterialFX;
	requires java.net.http;
	requires org.json;
	requires org.apache.httpcomponents.client5.httpclient5;
	requires org.apache.httpcomponents.core5.httpcore5;

	opens com.bitbybit.controllers to javafx.fxml;
	opens com.bitbybit.components to javafx.fxml;
	opens com.bitbybit.base to javafx.fxml;

	exports com.bitbybit;
}