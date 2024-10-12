package ru.urfu.sv.utils.result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionResultFactory {
    public static ActionResult emptySuccess() {
        return new ActionResult(true, "");
    }

    public static ActionResult firstAdminExist() {
        return new ActionResult(false, "Первый администратор уже существует");
    }

    public static ActionResult userExist(String username) {
        return new ActionResult(false, "Пользователь с логином %s уже существует", username);
    }

    public static ActionResult userNotExist(String username){
        return new ActionResult(false, "Пользователь с логином %s не существует", username);
    }

    public static ActionResult userCreated() {
        return new ActionResult(true, "Пользователь успешно создан");
    }

    public static ActionResult userCreatingError() {
        return new ActionResult(false, "Во время создания пользователя произошла ошибка");
    }

    public static ActionResult sessionExist() {
        return new ActionResult(false, "Такая пара уже существует");
    }

    public static ActionResult sessionNotExist() {
        return new ActionResult(false, "Такой пары не существует");
    }

    public static ActionResult sessionCreated() {
        return new ActionResult(true, "Пара успешно запланирована");
    }

    public static ActionResult sessionCreatingError() {
        return new ActionResult(false, "Во время планирования пары произошла ошибка");
    }

    public static ActionResult courseExist() {
        return new ActionResult(false, "Такой предмет уже существует");
    }

    public static ActionResult courseNotExist() {
        return new ActionResult(false, "Такой предмет не найден");
    }

    public static ActionResult courseCreated() {
        return new ActionResult(true, "Предмет успешно создан");
    }

    public static ActionResult courseCreatingError() {
        return new ActionResult(false, "Во время создания предмета произошла ошибка");
    }

    public static ActionResult professorExist() {
        return new ActionResult(false, "Такой преподаватель уже существует");
    }

    public static ActionResult professorNotExist(String username) {
        return new ActionResult(false, "Преподаватель с username=%s не найден", username);
    }

    public static ActionResult professorCreated() {
        return new ActionResult(true, "Преподаватель успешно создан");
    }

    public static ActionResult professorCreatingError() {
        return new ActionResult(false, "Во время создания преподавателя произошла ошибка");
    }

    public static ActionResult reviewExist() {
        return new ActionResult(false, "Такой отзыв уже существует");
    }

    public static ActionResult reviewNotExist() {
        return new ActionResult(false, "Такой отзыв не найден");
    }

    public static ActionResult reviewCreated(String reviewStr) {
        return new ActionResult(true, "Отзыв {} успешно создан", reviewStr);
    }

    public static ActionResult reviewCreatingError() {
        return new ActionResult(false, "Во время сохранения отзыва произошла ошибка");
    }

    public static ActionResult instituteExist() {
        return new ActionResult(false, "Такой институт уже существует");
    }
    public static ActionResult instituteNotExist() {
        return new ActionResult(false, "Такой институт не существует");
    }


    public static ActionResult instituteCreated() {
        return new ActionResult(true, "Институт успешно создан");
    }

    public static ActionResult instituteCreatingError() {
        return new ActionResult(false, "Во время создания института произошла ошибка");
    }

}
