#pragma once

namespace JanusSupport {

  class NativeAudioTrack {
   public:
     void setTrack(void* track) {
       this->_track = track;
     }
     void* getTrack() {
       return this->_track;
     }
   private:
     void* _track = nullptr;
  };

}  // namespace JanusSupport
