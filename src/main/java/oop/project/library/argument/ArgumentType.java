package oop.project.library.argument;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ArgumentType<T> {

    String description();

    T parse(String raw) throws ParseFailure;

    default ArgumentType<T> validate(Validator<? super T> validator) {
        ArgumentType<T> self = this;
        return new SimpleArgumentType<>(description(), raw -> {
            T value = self.parse(raw);
            validator.validate(value);
            return value;
        });
    }

    default ArgumentType<T> validate(Predicate<? super T> predicate, String message) {
        return validate(value -> {
            if (!predicate.test(value)) {
                throw new ParseFailure(message);
            }
        });
    }

    default <R> ArgumentType<R> map(String description, Function<? super T, ? extends R> mapper) {
        ArgumentType<T> self = this;
        return new SimpleArgumentType<>(description, raw -> mapper.apply(self.parse(raw)));
    }

}
