package og.hsweb.ezorm.param;

/**
 * Created by zhouhao on 16-4-19.
 */
public class UpdateParam<T> extends SqlParam<UpdateParam<T>> {
    private T data;

    public UpdateParam() {
    }

    public UpdateParam(T data) {
        this.data = data;
    }

    public UpdateParam<T> set(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> UpdateParam<T> build(T data) {
        return new UpdateParam<>(data);
    }

    public static <T> UpdateParam<T> build(T data, String condition, Object value) {
        return new UpdateParam<>(data).where(condition, value);
    }
}
