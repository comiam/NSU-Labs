#include "gtest/gtest.h"
#include "TritSet.h"
#include "Trit.h"

TEST(MemoryTests, ConstructorAllocationTest)
{
    for(int i = 0; i <= 1000; ++i)
    {
        TritSet set(i);

        size_t allocLength = set.capacity();
        EXPECT_GE(allocLength, i * 2 / 8 / sizeof(uint));
    }
}

TEST(MemoryTests, TritMemoryUnknown)
{
    TritSet set(6);
    set[0] = trit::True;

    EXPECT_TRUE(set[6] == trit::Unknown);
}

TEST(MemoryTests, TritMemory)
{
    TritSet set(6);
    set[0] = trit::True;
    set[1] = trit::False;
    set[2] = trit::True;
    set[3] = trit::True;
    set[4] = trit::Unknown;
    set[5] = trit::False;

    EXPECT_TRUE(set[0] == trit::True);
    EXPECT_TRUE(set[1] == trit::False);
    EXPECT_TRUE(set[2] == trit::True);
    EXPECT_TRUE(set[3] == trit::True);
    EXPECT_TRUE(set[4] == trit::Unknown);
    EXPECT_TRUE(set[5] == trit::False);
}

TEST(MemoryTests, UnknownOutOfBound)
{
    TritSet set(1000);
    size_t allocLength = set.capacity();

    set[1000000000] = trit::Unknown;
    EXPECT_EQ(allocLength, set.capacity());
}

TEST(MemoryTests, ComparingOutOfBound)
{
    TritSet set(1000);
    size_t allocLength = set.capacity();

    EXPECT_FALSE(set[2000000000] == trit::True);
    EXPECT_EQ(allocLength, set.capacity());
}

TEST(MemoryTests, OutOfBoundAllocation)
{
    TritSet set(1000);
    size_t allocLength = set.capacity();

    set[1000000000] = trit::True;
    EXPECT_LT(allocLength, set.capacity());
}

TEST(MemoryTests, ShrinkMemoryFree)
{
    bool condTrue = true;

    TritSet set(1000);
    set[100100] = trit::True;
    set[100100] = trit::Unknown;

    long a = 31;//76567 2 16 15 size-1 size 0 32 31

    set[a] = trit::False;

    set.shrink();
    uint x = ceil((2.0 * (a + 1)) / (8.0 * sizeof(uint)));

    EXPECT_EQ(x, set.capacity());
    EXPECT_EQ(a + 1, set.tritSize());
}

TEST(MemoryTests, tritOperationAllocation)
{
    TritSet setA(1000);
    TritSet setB(2000);

    TritSet setC = setA | setB;

    EXPECT_EQ(setB.capacity(), setC.capacity());
}

TEST(tritOperationTests, OrTest)
{
    TritSet setA(3);
    TritSet setB(3);

    setA[0] = trit::Unknown;
    setA[1] = trit::Unknown;
    setA[2] = trit::Unknown;

    setB[0] = trit::True;
    setB[1] = trit::False;
    setB[2] = trit::Unknown;

    TritSet setC = setA | setB;

    EXPECT_TRUE(setC[0] == trit::True);
    EXPECT_TRUE(setC[1] == trit::Unknown);
    EXPECT_TRUE(setC[2] == trit::Unknown);
}

TEST(tritOperationTests, AndTest)
{
    TritSet setA(3);
    TritSet setB(3);

    setA[0] = trit::Unknown;
    setA[1] = trit::Unknown;
    setA[2] = trit::Unknown;

    setB[0] = trit::True;
    setB[1] = trit::False;
    setB[2] = trit::Unknown;

    TritSet setC = setA & setB;

    EXPECT_TRUE(setC[0] == trit::Unknown);
    EXPECT_TRUE(setC[1] == trit::False);
    EXPECT_TRUE(setC[2] == trit::Unknown);
}

TEST(tritOperationTests, NotTest)
{
    TritSet setA(3);

    setA[0] = trit::True;
    setA[1] = trit::False;
    setA[2] = trit::Unknown;

    TritSet setC = !setA;

    EXPECT_TRUE(setC[0] == trit::False);
    EXPECT_TRUE(setC[1] == trit::True);
    EXPECT_TRUE(setC[2] == trit::Unknown);
}

TEST(OtherFunctionsTests, CardinalityFunctionTest)
{
    TritSet set(3);

    set[0] = trit::True;
    set[1] = trit::Unknown;
    set[2] = trit::False;

    EXPECT_EQ(set.cardinality(trit::False), 1);
    EXPECT_EQ(set.cardinality(trit::True), 1);
    EXPECT_EQ(set.cardinality(trit::Unknown), 1);
}

TEST(OtherFunctionsTests, CardinalityMapTest)
{
    TritSet set(11);

    set[0] = trit::True;
    set[1] = trit::Unknown;
    set[2] = trit::False;
    set[3] = trit::False;
    set[4] = trit::Unknown;
    set[5] = trit::False;
    set[6] = trit::Unknown;
    set[7] = trit::Unknown;
    set[8] = trit::Unknown;
    set[9] = trit::Unknown;
    set[10] = trit::Unknown;

    auto a = set.cardinality();

    EXPECT_EQ(a[trit::True], 1);
    EXPECT_EQ(a[trit::False], 3);
    EXPECT_EQ(a[trit::Unknown], 2);
}

TEST(OtherFunctionsTests, TrimTest)
{
    TritSet set(7);

    set[0] = trit::True;
    set[1] = trit::Unknown;
    set[2] = trit::False;
    set[3] = trit::False;
    set[4] = trit::Unknown;
    set[5] = trit::True;
    set[6] = trit::Unknown;

    set.trim(2);
    EXPECT_TRUE(set[2] == trit::Unknown && set[3] == trit::Unknown && set[4] == trit::Unknown && set[5] == trit::Unknown && set[6] == trit::Unknown);
}

TEST(OtherFunctionsTests, LenTest)
{
    TritSet set(4);

    set[0] = trit::True;
    set[1] = trit::Unknown;
    set[2] = trit::False;
    set[3] = trit::Unknown;

    EXPECT_EQ(set.length(), 3);
}