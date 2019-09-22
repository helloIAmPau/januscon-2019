#include "janus/uuid.h"

#ifdef ANDROID
#include "janus/defaults.h"
#endif

namespace Janus {

  std::string UuidImpl::create() {
#ifdef ANDROID
    auto guid = xg::newGuid(DEFAULT_ENV::get());
#else
    auto guid = xg::newGuid();
#endif

    return guid.str();
  }

}  // namespace Janus
