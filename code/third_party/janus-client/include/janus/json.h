#pragma once

#include <jansson.h>
#include <string>
#include <functional>
#include <memory>

namespace Janus {

  class JSON {
   public:
     JSON();
     explicit JSON(std::string json);
     explicit JSON(json_t* root);
     ~JSON();

     std::string serialize();
     std::string string(std::string field);
     void string(std::string field, std::string value);
     int64_t integer(std::string field);
     void integer(std::string field, int64_t number);
     bool boolean(std::string field);
     void boolean(std::string field, bool value);
     std::shared_ptr<JSON> object(std::string field);
     void object(std::string field, std::shared_ptr<JSON> value);

     void map(const std::function<void(std::string key, std::shared_ptr<JSON> value)>& mapper);
     bool hasKey(std::string field);
   private:
     json_t* _root = nullptr;
     json_t* _get(std::string field);
     void _set(std::string field, json_t* value);
  };

}  // namespace Janus
