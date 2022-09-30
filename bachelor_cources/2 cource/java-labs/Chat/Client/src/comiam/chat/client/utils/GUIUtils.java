package comiam.chat.client.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Popup;

public class GUIUtils
{
    public static void addUserToList(TextFlow userFlow, Pair<String, String> usr)
    {
        Text username0 = new Text("User: ");
        username0.setFont(Font.font(username0.getFont().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, 13));

        Text username1 = new Text(usr.getFirst() + "\n");
        username1.setFont(Font.font(username1.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 13));

        Text lastActive = new Text("Last active: " + usr.getSecond() + "\n");
        lastActive.setFont(Font.font(lastActive.getFont().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, 12));
        lastActive.setUnderline(true);

        userFlow.getChildren().addAll(username0, username1, lastActive);
    }

    public static void addMessage(TextFlow msgArea, String username, String date, String message)
    {
        if(username.equals("server"))
        {
            Label messageT = new Label(message);
            messageT.setFont(Font.font(messageT.getFont().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, 14));
            messageT.setTextFill(Color.RED);
            messageT.setMinWidth(msgArea.getPrefWidth());
            messageT.setAlignment(Pos.TOP_CENTER);
            setPopup(messageT, date, true);

            msgArea.getChildren().add(messageT);
        }else
        {
            Text usernameT = new Text((msgArea.getChildren().isEmpty() ? "" : "\n") + username + "\n");
            usernameT.setFont(Font.font(usernameT.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 13));
            msgArea.getChildren().add(usernameT);

            Text messageT = new Text(message);
            messageT.setFont(Font.font(messageT.getFont().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, 15));
            setPopup(messageT, date, true);

            msgArea.getChildren().add(messageT);
        }
    }

    public static TextFlow getTextFlow(ScrollPane parent)
    {
        TextFlow msgArea = new TextFlow();
        msgArea.setPrefHeight(306);
        msgArea.setPrefWidth(279);
        msgArea.setMaxWidth(Double.MAX_VALUE);
        msgArea.setMaxHeight(Region.USE_COMPUTED_SIZE);
        msgArea.setMinHeight(Region.USE_COMPUTED_SIZE);
        msgArea.setMinWidth(Region.USE_COMPUTED_SIZE);

        parent.vvalueProperty().bind(msgArea.heightProperty());

        return msgArea;
    }

    public static void setSmartScrollProperty(ScrollPane parent, TextFlow textFlow)
    {
        parent.vvalueProperty().addListener((ov, old_val, new_val) -> {
            if(new_val.floatValue() == parent.getVmax())
                parent.vvalueProperty().bind(textFlow.heightProperty());
            else
                parent.vvalueProperty().unbind();
        });
    }

    public static void setPopup(Node text, String popupMsg, boolean showOnLabel)
    {
        Label label = new Label(popupMsg);
        label.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        Popup popup = new Popup();
        popup.getContent().add(label);

        double offset = 5;
        text.setOnMouseMoved((e) -> {
            popup.setAnchorX(e.getScreenX() + offset * 2);
            popup.setAnchorY(e.getScreenY() + offset);
        });

        text.setOnMouseEntered(e -> {
            if(text instanceof Label)
            {
                if(computeTextWidth(((Label)text).getFont(), popupMsg) > ((Label)text).getWidth() || showOnLabel)
                    popup.show(text, e.getScreenX() + offset * 2, e.getScreenY());
            }else
                popup.show(text, e.getScreenX() + offset * 2, e.getScreenY());
        });
        text.setOnMouseExited(e -> popup.hide());
    }

    private static double computeTextWidth(Font font, String text)
    {
        Text theText = new Text(text);
        theText.setFont(font);
        return theText.getBoundsInLocal().getWidth();
    }
}
