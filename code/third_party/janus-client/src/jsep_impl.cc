#include "janus/jsep_impl.h"
#include <regex>

namespace Janus {

  JsepImpl::JsepImpl(const std::string& type, const std::string& sdp) {
    this->_type = type;
    this->_sdp = sdp;
  }

  std::string JsepImpl::type() {
    return this->_type;
  }

  std::string JsepImpl::sdp() {
    return this->_sdp;
  }

  bool JsepImpl::hasMedia(const std::string& media) {
    std::regex mediaRegex("\\bm=" + media + "\\b");

    return std::regex_search(this->sdp(), mediaRegex);
  }

  std::shared_ptr<Jsep> Jsep::create(const std::string& type, const std::string& sdp) {
    return std::make_shared<JsepImpl>(type, sdp);
  }

}  // namespace Janus
