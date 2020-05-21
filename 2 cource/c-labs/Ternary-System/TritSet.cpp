#include <bitset>
#include "TritSet.h"

inline void setBit(uint &x, unsigned int value, unsigned int index);
inline unsigned int getBit(uint x, unsigned int index);

size_t getTritCapInUInt()
{
    return ((8 * sizeof(uint)) / 2);
}

TritSet::tritReference TritSet::operator[](ull b)
{
    tritReference a(b, *this);
    return a;
}

TritSet TritSet::operator|(TritSet &a)
{
    auto c = TritSet(a.capacity() > this->capacity() ? a.capacity() : this->capacity());

    c.arr = a.capacity() > this->capacity() ? a.arr : this->arr;
    c.count = a.capacity() > this->capacity() ? a.count : this->count;
    size_t s = a.capacity() > this->capacity() ? this->arr.size() : a.arr.size();

    for(size_t i = 0; i < s; ++i)
        c.arr[i] |= a.capacity() > this->capacity() ? this->arr[i] : a.arr[i];

    return move(c);
}

TritSet TritSet::operator&(TritSet &a)
{
    auto c = TritSet(a.capacity() > this->capacity() ? a.capacity() : this->capacity());

    c.arr = a.capacity() > this->capacity() ? a.arr : this->arr;
    c.count = a.capacity() > this->capacity() ? a.count : this->count;
    size_t s = a.capacity() > this->capacity() ? this->arr.size() : a.arr.size();

    for(size_t i = 0; i < s; ++i)
        c.arr[i] &= a.capacity() > this->capacity() ? this->arr[i] : a.arr[i];

    return move(c);
}

TritSet TritSet::operator!()
{
    auto c = TritSet(this->capacity());
    c.arr = this->arr;
    c.count = this->count;

    for(uint &a : c.arr)
    {
        a = ~a;
        for(size_t i = 0; i < getTritCapInUInt(); ++i)
            if(!(a >> (i * 2u) & 1u) && (a >> (i * 2u + 1u) & 1u))
            {
                setBit(a, 1, i * 2u);
                setBit(a, 0, i * 2u + 1u);
            }
    }

    return move(c);
}

TritSet::TritSet(TritSet &&a) noexcept
{
    arr = move(a.arr);
    count = a.count;

    a.count = 0;
}

TritSet::TritSet(ull capacity)
{
    if(!unknownMask)
        for(size_t i = 0; i < 8 * sizeof(uint); ++i)
            unknownMask |= i % 2 == 0 ? 1u << i : 0;

    vector<uint> a(ceil((2.0 * capacity) / (8.0 * sizeof(uint))), unknownMask);

    count = capacity;
    arr = a;
}

ull TritSet::capacity()
{
    return ceil((2.0 * count) / (8.0 * sizeof(uint)));
}

trit TritSet::getValue(ull index0, uint index1) const
{
    if(index0 >= arr.size() || index1 >= getTritCapInUInt())
        throw invalid_argument("bad index at getValue(): index0 " + to_string(index0) + " vs containerSize " +
                               to_string(arr.size()) + ", tritIndex " + to_string(index1) +
                               " vs uint size " + to_string(getTritCapInUInt()));

    unsigned char value = 0;

    value |= getBit(arr[index0], index1 * 2);
    value |= getBit(arr[index0], index1 * 2 + 1) << 1u;

    switch(value)
    {
        case 0:
            return trit::False;
        case 1:
        case 2:
            return trit::Unknown;
        case 3:
            return trit::True;
        default:
            return trit::False;
    }
}

trit TritSet::getValue(ull index) const
{
    if(index >= count)
        throw invalid_argument(
                "bad index at setValue(): index " + to_string(index) + " vs tritSize " + to_string(count));

    ull indexEl = index / ((8.0 * sizeof(uint)) / 2.0);
    uint indexC = index % getTritCapInUInt();

    unsigned char value = 0;

    value |= getBit(arr[indexEl], indexC * 2);
    value |= getBit(arr[indexEl], indexC * 2 + 1) << 1u;

    switch(value)
    {
        case 0:
            return trit::False;
        case 1:
        case 2:
            return trit::Unknown;
        case 3:
            return trit::True;
        default:
            return trit::False;
    }
}

void TritSet::setValue(ull index, trit value)
{
    if(index >= count)
        throw invalid_argument(
                "bad index at setValue(): index " + to_string(index) + " vs tritSize " + to_string(count));


    ull indexEl = index / ((8.0 * sizeof(uint)) / 2.0);
    uint indexC = index % getTritCapInUInt();
    uint intVal;
    switch(value)
    {
        case trit::False:
            intVal = 0;
            break;
        case trit::Unknown:
            intVal = 1;
            break;
        case trit::True:
            intVal = 3;
            break;
    }

    setBit(arr[indexEl], intVal & 1u, indexC * 2);
    setBit(arr[indexEl], (intVal >> 1u) & 1u, indexC * 2 + 1);
}

void TritSet::setValue(ull index0, uint index1, trit value)
{
    if(index0 >= arr.size() || index1 >= ((8.0 * sizeof(uint)) / 2.0))
        throw invalid_argument("bad index at setValue()\n");

    uint intVal;
    switch(value)
    {
        case trit::False:
            intVal = 0;
            break;
        case trit::Unknown:
            intVal = 1;
            break;
        case trit::True:
            intVal = 3;
            break;
    }

    setBit(arr[index0], intVal & 1u, index1 * 2);
    setBit(arr[index0], (intVal >> 1u) & 1u, index1 * 2 + 1);
}

void TritSet::shrink()
{
    ull a = arr.size();
    for(uint i = a - 1;; --i)
    {
        if(arr[i] ^ unknownMask)
        {
            arr.resize(i + 1);
            count -= count % getTritCapInUInt() + 1;
            count -= (a - i - 2) * getTritCapInUInt();
            break;
        }
        if(!i)
        {
            arr.resize(0);
            count = 0;
            break;
        }
    }

    uint b = arr[arr.size() - 1];
    for(uint i = count % getTritCapInUInt();; --i)
        if((b >> (i * 2u + 1u) & 1u) || !(b >> (i * 2u) & 1u))
        {
            count -= count % getTritCapInUInt() - i - 1;
            break;
        }
}

void TritSet::trim(ull lastIndex)
{
    if(lastIndex >= count)
        return;

    ull indexEl = lastIndex / ((8.0 * sizeof(uint)) / 2.0);

    for(size_t i = lastIndex % getTritCapInUInt(); i < getTritCapInUInt(); ++i)
        setValue(indexEl, i, trit::Unknown);

    for(size_t j = indexEl + 1; j < arr.size(); ++j)
        arr[j] = unknownMask;
}

ull TritSet::length()
{
    for(uint i = arr.size() - 1;; --i)
    {
        for(uint j = getTritCapInUInt() - 1;; --j)
        {
            if((arr[i] >> (j * 2u + 1u) & 1u) || !(arr[i] >> (j * 2u) & 1u))
                return i * getTritCapInUInt() + j + 1;
            if(!j)
                break;
        }
        if(!i)
            break;
    }
    return 0;
}

ull TritSet::cardinality(trit value)
{
    bool enableCount = value != trit::Unknown;

    ull counter = 0;
    for(uint i = arr.size() - 1;; --i)
    {
        for(uint j = getTritCapInUInt() - 1;; --j)
        {
            if(getValue(i, j) == value && enableCount)
                counter++;
            else if(value == trit::Unknown && ((arr[i] >> (j * 2u + 1u) & 1u) || !(arr[i] >> (j * 2u) & 1u)) && !enableCount)
                enableCount = true;
            if(!j)
                break;
        }
        if(!i)
            break;
    }

    return counter;
}

unordered_map<trit, int, TritSet::tritHash> TritSet::cardinality()
{
    unordered_map<trit, int, TritSet::tritHash> tritInfo;
    tritInfo.insert(make_pair<trit, int>(trit::True, cardinality(trit::True)));
    tritInfo.insert(make_pair<trit, int>(trit::False, cardinality(trit::False)));
    tritInfo.insert(make_pair<trit, int>(trit::Unknown, cardinality(trit::Unknown)));

    return tritInfo;
}

ull TritSet::tritSize()
{
    return count;
}

unsigned int getBit(uint x, unsigned int index)
{
    return (x >> index) & 1u;
}

inline void setBit(uint &x, unsigned int value, unsigned int index)
{
    if(value)
        x |= 1u << index;
    else
        x &= ~(1u << index);
}