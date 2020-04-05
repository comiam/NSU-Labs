#pragma once
#include <tuple>
#include <functional>
#include <iostream>
#include <sstream>

template<int index, typename Callback, typename... Args>
struct iterate
{
    static void next(std::tuple<Args...> &t, Callback callback, std::ostream &stream)
    {
        iterate<index - 1, Callback, Args...>::next(t, callback, stream);
        callback(index, std::get<index>(t), stream);
    }
};

template<typename Callback, typename... Args>
struct iterate<0, Callback, Args...>
{
    static void next(std::tuple<Args...> &t, Callback callback, std::ostream &stream)
    {
        callback(0, std::get<0>(t), stream);
    }
};

template<typename Callback, typename... Args>
struct iterate<-1, Callback, Args...>
{
    static void next(std::tuple<Args...> &t, Callback callback, std::ostream &stream){}
};

template<typename Callback, typename... Args>
void forEach(std::tuple<Args...> &t, Callback callback, std::ostream &stream)
{
    int const t_size = std::tuple_size<std::tuple<Args...>>::value;

    iterate<t_size - 1, Callback, Args...>::next(t, callback, stream);
}

struct callback
{
    template<typename T>
    void operator()(int index, T &&t, std::ostream &stream)
    {
        stream << t << " ";
    }
};

template<typename _CharT, typename _Traits, typename... Args>
inline std::basic_ostream<_CharT, _Traits> &operator<<(std::basic_ostream<_CharT, _Traits>& stream, std::tuple<Args...> &t)
{
    forEach(t, callback(), stream);

    return stream;
}

//This code is drenched in my blood...
namespace parser_utils
{
    template <class Dest>
    class lexical_cast
    {
        Dest value;
    public:
        lexical_cast(const std::string &src)
        {
            std::stringstream s;
            s << src;
            s >> value;
            if(!s)
                throw std::exception();
        }

        operator const Dest&() const {
            return value;
        }

        operator Dest&() {
            return value;
        }
    };

    template <>
    class lexical_cast<std::string>
    {
        std::string value;
    public:
        lexical_cast(const std::string &src): value(src) {}

        operator const std::string&() const {
            return value;
        }

        operator std::string&() {
            return value;
        }
    };

    template<int index, typename Callback, typename... Args>
    struct iterate
    {
        static void next(std::tuple<Args...> &t, Callback callback, std::vector<std::string>::iterator& it)
        {
            iterate<index - 1, Callback, Args...>::next(t, callback, it);
            callback(index - 1, std::get<index>(t), it);
        }
    };

    template<typename Callback, typename... Args>
    struct iterate<0, Callback, Args...>
    {
        static void next(std::tuple<Args...> &t, Callback callback, std::vector<std::string>::iterator& it)
        {
            callback(0, std::get<0>(t), it);
        }
    };

    template<typename Callback, typename... Args>
    struct iterate<-1, Callback, Args...>
    {
        static void next(std::tuple<Args...> &t, Callback callback, std::vector<std::string>::iterator& it){}
    };


    template<typename Callback, typename... Args>
    void forEach(std::tuple<Args...> &t, Callback callback, std::vector<std::string>::iterator& it)
    {
        int const t_size = std::tuple_size<std::tuple<Args...>>::value;

        iterate<t_size - 1, Callback, Args...>::next(t, callback, it);
    }

    struct callback
    {
        template<typename T>
        void operator()(int index, T &t, std::vector<std::string>::iterator& it)
        {
            t = lexical_cast<T>(*it);
            ++it;
        }
    };

    template <typename ...Args>
    void parse(std::tuple<Args...> &tuple, std::vector<std::string>::iterator& it)
    {
        forEach(tuple, callback(), it);
    }
}