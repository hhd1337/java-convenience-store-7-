package store;

import store.controller.ConvenienceStoreController;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    public static void main(String[] args) {
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();

        ConvenienceStoreController convenienceStoreController = new ConvenienceStoreController(inputView, outputView);
        convenienceStoreController.run();
    }
}
