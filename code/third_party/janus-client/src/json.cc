#include "janus/json.h"

namespace Janus {

  JSON::JSON(json_t* root) {
    if(root == nullptr) {
      this->_root = json_object();

      return;
    }

    this->_root = root;
    json_incref(root);
  }

  JSON::JSON() {
    this->_root = json_object();
  }

  JSON::JSON(std::string json) {
    json_error_t error;

    this->_root = json_loads(json.c_str(), 0, &error);
  }

  JSON::~JSON() {
    json_decref(this->_root);
  }

  std::string JSON::serialize() {
    auto serialized = json_dumps(this->_root, 0);
    std::string wrapped(serialized);
    free(serialized);

    return wrapped;
  }

  std::string JSON::string(std::string field) {
    auto obj = this->_get(field);

    if(!json_is_string(obj)) {
      return "";
    }

    return json_string_value(this->_get(field));
  }

  void JSON::string(std::string field, std::string value) {
    auto obj = json_string(value.c_str());
    this->_set(field, obj);
  }

  int64_t JSON::integer(std::string field) {
    auto obj = this->_get(field);

    if(!json_is_integer(obj)) {
      return -999;
    }

    return json_integer_value(this->_get(field));
  }

  void JSON::integer(std::string field, int64_t value) {
    auto obj = json_integer(value);
    this->_set(field, obj);
  }

  bool JSON::boolean(std::string field) {
    return json_is_true(this->_get(field));
  }

  void JSON::boolean(std::string field, bool value) {
    auto obj = json_boolean(value);
    this->_set(field, obj);
  }

  std::shared_ptr<JSON> JSON::object(std::string field) {
    auto obj = this->_get(field);

    return std::make_shared<JSON>(obj);
  }

  void JSON::object(std::string field, std::shared_ptr<JSON> value) {
    json_incref(value->_root);
    this->_set(field, value->_root);
  }

  void JSON::map(const std::function<void(std::string key, std::shared_ptr<JSON> value)>& mapper) {
    const char* key;
    json_t* value;

    json_object_foreach(this->_root, key, value) {
      mapper(key, std::make_shared<JSON>(value));
    }
  }

  bool JSON::hasKey(std::string field) {
    return this->_get(field) != nullptr;
  }

  json_t* JSON::_get(std::string field) {
    return json_object_get(this->_root, field.c_str());
  }

  void JSON::_set(std::string field, json_t* value) {
    json_object_set(this->_root, field.c_str(), value);
    json_decref(value);
  }

}  // namespace Janus
