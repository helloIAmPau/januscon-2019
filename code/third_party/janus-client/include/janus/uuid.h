#pragma once

#include <string>
#include <crossguid/guid.hpp>

namespace Janus {

  class Uuid {
   public:
     virtual std::string create() = 0;
  };

  class UuidImpl : public Uuid {
   public:
     std::string create();
  };

}  // namespace Janus
