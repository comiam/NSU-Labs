#include <stdio.h>
#include <stdlib.h>
#include <locale.h>
#include <string.h>

// TODO реализуй меня
// Ищет "иголку" (needle) в "стоге" (haystack), имитирует стандартную функцию strstr
// Возвращает или
//	* _указатель_ на то место, где найдена "иголка" (аналогично стандартной функции strstr)
//	* NULL, если "иголка" не найдена
char* my_strstr(char* haystack, char* needle) {
    if(haystack == NULL || needle == NULL)
        return NULL;
    if(strlen(haystack) < strlen(needle))
        return NULL;

    int cmp;
    size_t end = strlen(haystack) - strlen(needle) + 1;
    size_t current;

    for (int i = 0; i < end; ++i)
    {
        cmp = 1;
        current = i + strlen(needle);
        for (int j = i; j < current; ++j)
            if(haystack[j] != needle[j - i])
            {
                cmp = 0;
                break;
            }
        if(cmp)
            return &haystack[i];
    }
    return NULL;
}


//********* Тесты *******************************************
int error_count;
#define check_expr(function, haystack, needle, expected) {\
	char* result = function(haystack, needle);\
	if (result != (expected)) {\
		printf("Тест на строке %d упал " #function "(" #haystack ", " #needle ") \n", __LINE__);\
		printf("\tожидался указатель %p, получился %p", expected, result);\
		if (expected && result) {\
			ptrdiff_t expected_offset = (char*) expected - (char*) haystack;\
			ptrdiff_t resulting_offset = (char*) result - (char*) haystack;\
			printf(", ожидали отступ от начала строки %d, получился %d", expected_offset, resulting_offset);\
						}\
		printf("\n");\
		error_count++;\
			}\
}

int error_count = 0;

void test_function(char* (function)(const char*, const char*)) {
    printf("\nГруппа 1: защита от ошибок\n");
    {
        check_expr(function, NULL, NULL, NULL);
        check_expr(function, "ABC", NULL, NULL);
        check_expr(function, NULL, "ABC", NULL);
    }

    printf("\nГруппа 2: найдено\n");
    {
        char* short_haystack = "ABCD";
        char* exact_needle = "ABCD";
        check_expr(function, short_haystack, exact_needle, short_haystack);

        char* needle_at_start = "ABC";
        check_expr(function, short_haystack, needle_at_start, short_haystack);

        char* off_by_one_needle = "BCD";
        check_expr(function, short_haystack, off_by_one_needle, short_haystack + 1);

        char* long_haystack = "Voluptatibus reiciendis nisi debitis porro non provident rerum nihil.";
        char* matching_needle = "provident";
        check_expr(function, long_haystack, matching_needle, long_haystack + 47);

        char* needle_at_the_end = "nihil.";
        check_expr(function, long_haystack, needle_at_the_end, long_haystack + 63);
    }

    printf("\nГруппа 3: не найдено\n");
    {
        char* short_haystack = "ABC";
        char* long_needle = "HELLO, WORLD";
        check_expr(function, short_haystack, long_needle, NULL);

        char* haystack = "ABCDEFGHIJKLMNO";
        char* not_found_needle = "HELLO";
        check_expr(function, haystack, not_found_needle, NULL);

        char* long_haystack = "Voluptatibus reiciendis nisi debitis porro non provident rerum nihil.";
        char* almost_matching_needle = "providend";
        check_expr(function, long_haystack, almost_matching_needle, NULL);
    }
}

int main() {
    system("chcp 65001");
    test_function(my_strstr);

    if (error_count) {
        printf("\n\n!!!!!!!!!! Обнаружены ошибки (см. выше), исправьте и попробуйте снова !!!!!!!!!!!!!!!\n");
        return 1;
    } else {
        printf("\n\n************ Все тесты прошли *************\n");
        return 0;
    }
}