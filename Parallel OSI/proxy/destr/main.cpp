#include <iostream>

using namespace std;

class A
{
public:
    A()
    {
        cout << "A created" << endl;
    };
    ~A()
    {
        cout << "A destroyed" << endl;
    }
};

class B
{
public:
    B()
    {
        cout << "B created" << endl;
    };
    ~B()
    {
        cout << "B destroyed" << endl;
    }
};

class C: public A, public B
{
public:
    C()
    {
        cout << "C created" << endl;
    };
    ~C()
    {
        cout << "C destroyed" << endl;
    }
};

int main()
{
    C s;
    return 0;
}
