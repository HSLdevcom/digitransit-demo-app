package fi.hsl.demoapp.util;

public class ViewState<T> {
    public enum State { LOADING, REFRESHING, CONTENT, ERROR }

    public ViewState.State state;
    public T content;
    public String error;

    private ViewState(ViewState.State state, T content, String error) {
        this.state = state;
        this.content = content;
        this.error = error;
    }

    public static ViewState loading() {
        return new ViewState(ViewState.State.LOADING, null, null);
    }

    public static <T> ViewState refreshing(T content) {
        return new ViewState(ViewState.State.REFRESHING, content, null);
    }

    public static <T> ViewState content(T content) {
        return new ViewState(ViewState.State.CONTENT, content, null);
    }

    public static ViewState error(String error) {
        return new ViewState(ViewState.State.ERROR, null, error);
    }
}
