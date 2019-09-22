#pragma once

#include "janus/jsep.hpp"
#include <string>

namespace Janus {

  class JsepImpl : public Jsep {
   public:
     JsepImpl(const std::string& type, const std::string& sdp);

     std::string type();
     std::string sdp();
     bool hasMedia(const std::string& media);

   private:
     std::string _type = "";
     std::string _sdp = "";
  };

}  // namespace Janus
