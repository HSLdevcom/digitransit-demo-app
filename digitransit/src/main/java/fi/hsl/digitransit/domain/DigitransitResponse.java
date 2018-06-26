package fi.hsl.digitransit.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

//Holder class for the data retrieved from Digitransit API
public class DigitransitResponse<T> {
    //Retrieved data, examples of possible types: Stop[], StopAtDistanceConnection
    private T value;

    public DigitransitResponse(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitransitResponse<?> that = (DigitransitResponse<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    //Gson deserializer for Digitransit responses
    public static class DigitransitResponseDeserializer<T> implements JsonDeserializer<DigitransitResponse<T>> {
        private Class<T> klass;

        public DigitransitResponseDeserializer(Class<T> klass) {
            this.klass = klass;
        }

        @Override
        public DigitransitResponse<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
            //Get the first element from JSON object "data"
            JsonElement value = data.entrySet().iterator().next().getValue();

            //Parse JSON element to a Java object with the specified type
            return new DigitransitResponse<T>((T)context.deserialize(value, klass));
        }
    }

    public static Type createType(final Class<?> parameter) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {parameter};
            }

            @Override
            public Type getRawType() {
                return DigitransitResponse.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
