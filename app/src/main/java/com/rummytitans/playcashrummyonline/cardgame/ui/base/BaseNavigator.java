package com.rummytitans.playcashrummyonline.cardgame.ui.base;


public interface BaseNavigator {

    void goBack();

    void handleError(Throwable throwable);

    void showError(String message);

    void showError(Integer message);

    void showMessage(String message);

    void logoutUser();

    String getStringResource(int resourseId);

}
