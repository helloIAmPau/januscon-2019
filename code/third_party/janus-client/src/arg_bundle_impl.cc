#include "janus/arg_bundle_impl.h"

namespace Janus {

  std::shared_ptr<ArgBundle> ArgBundle::create() {
    return std::make_shared<ArgBundleImpl>();
  }

  void ArgBundleImpl::setInt(const std::string& key, int32_t value) {
    this->_ints.insert({ key, value });
  }

  int32_t ArgBundleImpl::getInt(const std::string& key) {
    return this->_getter<int32_t>(this->_ints, key, -999);
  }

  void ArgBundleImpl::setLong(const std::string& key, int64_t value) {
    this->_longs.insert({ key, value });
  }

  int64_t ArgBundleImpl::getLong(const std::string& key) {
    return this->_getter<int64_t>(this->_longs, key, -999);
  }

  void ArgBundleImpl::setString(const std::string& key, const std::string& value) {
    this->_strings.insert({ key, value });
  }

  std::string ArgBundleImpl::getString(const std::string& key) {
    return this->_getter<std::string>(this->_strings, key, "");
  }

  void ArgBundleImpl::setBool(const std::string& key, bool value) {
    this->_bools.insert({ key, value });
  }

  bool ArgBundleImpl::getBool(const std::string& key) {
    return this->_getter<bool>(this->_bools, key, false);
  }

}  // namespace Janus
