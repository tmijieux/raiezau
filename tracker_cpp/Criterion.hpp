#ifndef CRITERION_H
#define CRITERION_H

#include <vector>
#include <memory>
#include <sstream>
#include <string>

class CriterionVec;
class CriterionBase;
template <typename PRED_T> class Criterion;

#include "./File.hpp"
#include "./FileMgr.hpp"

class CriterionBase {
public:
    virtual ~CriterionBase() {}
    virtual bool match(std::string) = 0;
    virtual FileSet applyCriterion(std::string, const FileSet&) = 0;
};

class CriterionVec : public std::vector<std::shared_ptr<CriterionBase>> {
public:
    static CriterionVec criterions;
    static CriterionVec initCriterions();
};

template<typename PRED_U>
static Criterion<PRED_U>*
mCriterion(std::string delimiter, std::string keyword, PRED_U predicate)
{
    return new Criterion<PRED_U>(delimiter, keyword, predicate);
}
    
template<typename PRED_T>
class Criterion : public CriterionBase {
public:
    
    Criterion(std::string delimiter, std::string keyword, PRED_T predicate):
        _delim(delimiter), _keyword(keyword), _predicate(predicate)
    {
    }
    
    //Criterion(Criterion&) = delete;

    bool match(std::string value)
    {
        size_t pos;
        pos = value.find(_delim);
        return pos != std::string::npos && value.substr(0, pos) == _keyword;
    }

    FileSet applyCriterion(std::string value, const FileSet &s)
    {
        size_t pos = value.find(_delim);
        std::string v = value.substr(pos+_delim.length(), std::string::npos);
        std::stringstream ss(v);
        std::string argument;
        getline(ss, argument, '"');
        FileSet r;

        for (auto it = s.begin(); it != s.end(); ++it) {
            if (_predicate(*it, argument))
                r.insert(*it);
        }
        return r;
    }

private:
    std::string _delim;
    std::string _keyword;
    PRED_T _predicate;
};

#endif //CRITERION_H
