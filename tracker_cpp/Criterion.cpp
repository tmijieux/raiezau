#include <string>
#include "./Criterion.hpp"

using namespace std;

class rz_exception : public exception {
public:
    rz_exception(const std::string s): _s(s) {}
    const char *what() const noexcept override { return (_s).c_str(); }
private:
    string _s;
};

CriterionVec CriterionVec::criterions = CriterionVec::initCriterions();

template<size_t (File::*field)() const>
struct GEQ {
    bool operator ()(File *f, string ssize)
    {
        return (f->*field)() >= (size_t) stoul(ssize);
    }
};

template<size_t (File::*field)() const>
struct LEQ {
    bool operator ()(File *f, string ssize)
    {
        return (f->*field)() <= (size_t) stoul(ssize);
    }
};

template<size_t (File::*field)() const>
struct GT {
    bool operator ()(File *f, string ssize)
    {
        return (f->*field)() > (size_t) stoul(ssize);
    }
};

template<size_t (File::*field)() const>
struct LT {
    bool operator ()(File *f, string ssize)
    {
        return (f->*field)() < (size_t) stoul(ssize);
    }
};

template<size_t (File::*field)() const, bool reversed = false>
struct EQI {
    bool operator() (File *f, string ssize)
    {
        size_t s = stoul(ssize);
        return (s == (f->*field)()) != reversed;
    }
};

template<std::string (File::*field)() const, bool reversed = false>
struct EQS {
    bool operator() (File *f, string name)
    {
        return (name == (f->*field)()) != reversed;
    }
};

template<size_t (File::*field)() const>
static void make_num_field(CriterionVec &v, string name)
{
    typedef shared_ptr<CriterionBase> _;
    
    LT<field> lt; GT<field> gt; LEQ<field> leq; GEQ<field> geq;
    EQI<field, true> eq; EQI<field, false> neq;
    
    v.push_back(_(mCriterion("=\"", name, eq)));
    v.push_back(_(mCriterion("!=\"", name, neq)));
    
    v.push_back(_(mCriterion(">=\"", name, geq)));
    v.push_back(_(mCriterion("<=\"", name, leq)));
    v.push_back(_(mCriterion(">\"", name, gt)));
    v.push_back(_(mCriterion("<\"", name, lt)));
}


template<string (File::*field)() const>
static void make_string_field(CriterionVec &v, string name)
{
    typedef shared_ptr<CriterionBase> _;

    EQS<field, true> neq;
    EQS<field, false> eq;
    v.push_back(_(mCriterion("!=\"", name, neq)));
    v.push_back(_(mCriterion("=\"", name, eq)));
}

CriterionVec CriterionVec::initCriterions()
{
    CriterionVec v;

    ::make_num_field<&File::size>(v, "filesize");
    ::make_num_field<&File::pieceSize>(v, "piecesize");
    ::make_string_field<&File::name>(v, "filename");
    ::make_string_field<&File::hash>(v, "hash");
    
    return v;
}
