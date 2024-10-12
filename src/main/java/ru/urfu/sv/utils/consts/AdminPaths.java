package ru.urfu.sv.utils.consts;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminPaths {

    private static final List<Path> ADMIN_PATHS = List.of(
            new Path("Открыть рейтинг институтов", "/rating/institutes"),
            new Path("Просмотреть пользователей", "/admin/users/list"),
            new Path("Создать пользователя", "/admin/users/create"),
            new Path("Создать пользователей из файла", "admin/users/create-from-file"),
            new Path("Создать институт", "/institutes/create"),
            new Path("Скачать отчет", "/reviews/download-report-xslx")
    );

    public static List<Path> getPaths(){
        return ADMIN_PATHS;
    }

    public record Path(String name, String path) {
    }
}
