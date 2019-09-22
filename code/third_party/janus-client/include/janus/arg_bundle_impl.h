#pragma once

#include <string>
#include <unordered_map>
#include <memory>
#include "janus/arg_bundle.hpp"

namespace Janus {

  class ArgBundleImpl : public ArgBundle {
   public:
     void setString(const std::string& key, const std::string& value);
     void setInt(const std::string& key, int32_t value);
     void setLong(const std::string& key, int64_t value);
     void setBool(const std::string& key, bool value);
     std::string getString(const std::string& key);
     int32_t getInt(const std::string& key);
     int64_t getLong(const std::string& key);
     bool getBool(const std::string& key);

   private:
     // these can be converted into an single map of type string, void*
     std::unordered_map<std::string, std::string> _strings;
     std::unordered_map<std::string, int32_t> _ints;
     std::unordered_map<std::string, int64_t> _longs;
     std::unordered_map<std::string, bool> _bools;

     template <typename T>
     T _getter(const std::unordered_map<std::string, T>& items, const std::string& key, const T& DEFAULT) {
       T item = DEFAULT;

       auto iterator = items.find(key);
       if(iterator != items.end()) {
         item = iterator->second;
       }

       return item;
     }
  };

}  // namespace Janus
