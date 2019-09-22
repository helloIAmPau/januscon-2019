#include <pthread.h>
#include <unistd.h>
#include "janus/protocol.hpp"
#include "janus/protocol_factory.hpp"
#include "janus/media.hpp"
#include "janus/platform.hpp"
#include "janus/ready_state.hpp"
#include "janus/janus_delegate.hpp"
#include "janus/command.hpp"
#include "janus/info.hpp"
#include "janus/arg_bundle.hpp"
#include "janus/uuid.h"
#include "janus/janus_event.hpp"
#include "janus/janus_plugin_info.hpp"
#include "janus/messages.h"
#include "janus/janus_api.h"
#include "janus/conf.h"
#include "janus/signaling.hpp"
#include "janus/http.h"
#include "janus/plugin_factory.hpp"
#include "janus/jsep.hpp"
#include "janus/candidate.hpp"
#include "janus/constraints.hpp"
#include "janus/json.h"
#include "janus/peer_factory.hpp"
#include "janus/peer.hpp"
#include "janus/janus_signaling.h"
#include "janus/plugin.hpp"
#include "janus/ice_server.hpp"
#include "janus/plugin_delegate.hpp"
#include "janus/native_video_track.h"
#include "janus/local_video_track.hpp"
#include "janus/remote_video_track.hpp"
#include "janus/local_audio_track.hpp"

namespace Janus {

  MATCHER(Any, "") {
    return true;
  }

  MATCHER_P(IsThePluginFactory, id, "") {
    return arg->info().id == id;
  }

  MATCHER_P(IsAnswerWithBody, sdp, "") {
    return arg->type() == "answer" && arg->sdp() == sdp;
  }

  MATCHER_P(IsOfferWithBody, sdp, "") {
    return arg->type() == "offer" && arg->sdp() == sdp;
  }

  MATCHER(IsEmptySdp, "") {
    return arg->type() == "" && arg->sdp() == "";
  }

  MATCHER_P4(ConstraintsHaveValues, audio, video, receiveAudio, receiveVideo, "") {
    return arg.audio == audio && arg.video == video && arg.offerToReceiveAudio == receiveAudio && arg.offerToReceiveVideo == receiveVideo;
  }

  MATCHER_P(IsAttachMessageWithId, id, "") {
    return arg->janus() == "attach" && arg->template as<Attach>()->plugin() == id;
  }

  MATCHER_P2(IsPluginMessage, body, handleId, "") {
    auto converted = arg->template as<PluginMessage>();
    return arg->janus() == "message"
      && converted->handleId() == handleId
      && converted->body()->serialize() == body->serialize();
  }

  MATCHER_P3(IsJsepMessage, body, jsep, handleId, "") {
    auto converted = arg->template as<JsepMessage>();
    return arg->janus() == "message"
      && converted->handleId() == handleId
      && converted->body()->serialize() == body->serialize()
      && converted->jsep()->type() == jsep->type()
      && converted->jsep()->sdp() == jsep->sdp();
  }

  MATCHER_P2(IsTrickleMessage, candidate, handleId, "") {
    auto converted = arg->template as<Trickle>();

    if(candidate.completed == true) {
      return arg->janus() == "trickle" && converted->handleId() == handleId && converted->completed() == true;
    }

    return arg->janus() == "trickle" && converted->handleId() == handleId && converted->candidate().sdpMid == candidate.sdpMid && converted->candidate().sdpMLineIndex == candidate.sdpMLineIndex && converted->candidate().candidate == candidate.candidate;
  }

  MATCHER_P(HasBody, body, "") {
    return arg.body == body;
  }

  MATCHER_P(IsType, type, "") {
    auto body = std::make_shared<JSON>(arg.body);
    return body->string("janus") == type;
  }

  MATCHER_P(IsMessage, type, "") {
    return std::make_shared<Message>(arg)->janus() == type;
  }

  MATCHER_P2(IsMessageWithHandleId, type, id, "") {
    return arg->janus() == type && arg->template as<Handle>()->handleId() == id;
  }

  MATCHER_P2(HasConf, key, value, "") {
    return arg.at(key) == value;
  }

  MATCHER_P(HasError, msg, "") {
    return arg.reason == msg;
  }

  MATCHER_P(MatchPluginInfo, plugin, "") {
    return arg.name == plugin.name && arg.id == plugin.id && arg.version == plugin.version;
  }

  MATCHER_P2(EventHasJsonField, key, value, "") {
    return std::make_shared<JSON>(arg->data())->string(key) == value;
  }

  MATCHER_P(HasContextFields, context, "" ) {
    return std::all_of(context.begin(), context.end(), [&](std::pair<std::string, std::string> pair){ return arg->getString(pair.first) == pair.second; });
  }

  ACTION_P(SlowReturn, value) {
    usleep(200000);
    return value;
  }

  namespace Mocks {

    class TransportMock : public Transport {
     public:
       TransportMock() : Transport(nullptr) {};
       MOCK_METHOD0(get, void());
       MOCK_METHOD1(request, void(const std::shared_ptr<Message>& request));
       MOCK_METHOD2(request, void(const std::shared_ptr<Message>& request, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD0(type, TransportType());
       MOCK_METHOD0(close, void());
    };

    class TransportFactoryMock : public TransportFactory {
     public:
       MOCK_METHOD4(create, std::shared_ptr<Transport>(const TransportType& type, const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<TransportDelegate>& delegate));
    };

    class DelegateMock : public TransportDelegate {
     public:
       MOCK_METHOD2(onMessage, void(const std::string& response, const std::shared_ptr<ArgBundle>& context));
    };

    class UuidMock : public Uuid {
     public:
       MOCK_METHOD0(create, std::string());
    };

    class HttpMock : public Http {
     public:
       MOCK_METHOD2(get, Response(std::string path, const Request& request));
       MOCK_METHOD2(post, Response(std::string path, const Request& request));
       MOCK_METHOD1(get, Response(std::string path));
       MOCK_METHOD1(post, Response(std::string path));
       MOCK_METHOD0(interrupt, void());
    };

    class HttpFactoryMock : public HttpFactory {
     public:
       MOCK_METHOD1(create, std::shared_ptr<Http>(const std::string& basePath));
    };

    class ProtocolMock : public Protocol {
     public:
       MOCK_METHOD0(info, Info());
       MOCK_METHOD0(close, void());
       MOCK_METHOD0(readyState, ReadyState());
       MOCK_METHOD0(plugins, std::vector<JanusPluginInfo>());
       MOCK_METHOD2(attach, void(const std::string& pluginId, const std::shared_ptr<ArgBundle>& context));
    };

    class ProtocolFactoryMock : public ProtocolFactory {
     public:
       MOCK_METHOD3(bootstrap, std::shared_ptr<Protocol>(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Platform>& platform, const std::shared_ptr<JanusDelegate>& delegate));
    };

    class PlatformMock : public Platform {
     public:
       PlatformMock() {}
       MOCK_METHOD1(registerProtocolFactory, void(const std::shared_ptr<ProtocolFactory>& factory));
       MOCK_METHOD2(getProtocol, std::shared_ptr<Protocol>(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<JanusDelegate>& delegate));
       MOCK_METHOD1(registerPeerFactory, void(const std::shared_ptr<PeerFactory>& factory));
       MOCK_METHOD2(createPeer, std::shared_ptr<Peer>(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Plugin>& delegate));
       MOCK_METHOD1(registerPluginFactory, void(const std::shared_ptr<PluginFactory>& pluginFactory));
       MOCK_METHOD1(getPlugin, std::shared_ptr<Plugin>(const std::string& id));
       MOCK_METHOD0(getPlugins, std::vector<JanusPluginInfo>());
    };

    class JanusDelegateMock : public JanusDelegate {
      public:
        MOCK_METHOD1(onJanusError, void(const JanusError& error));
        MOCK_METHOD3(onPluginEnabled, void(const std::string& id, const std::shared_ptr<Plugin>& handle, const std::shared_ptr<ArgBundle>& context));
    };

    class PeerFactoryMock : public PeerFactory {
     public:
       MOCK_METHOD2(create, std::shared_ptr<Peer>(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Plugin>& delegate));
    };

    class PluginFactoryMock : public PluginFactory {
     public:
       MOCK_METHOD0(id, std::string());
       MOCK_METHOD1(create, std::shared_ptr<Plugin>(const std::shared_ptr<Plugin>& plugin));
    };

    class PluginMock : public Plugin {
     public:
       MOCK_METHOD2(onEvent, void(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD2(dispatch, void(const std::string& name, const std::shared_ptr<ArgBundle>& data));
       MOCK_METHOD2(init, void(const std::shared_ptr<Signaling>& signaling, const std::shared_ptr<Peer>& peer));
       MOCK_METHOD2(onOffer, void(const std::string& sdp, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD2(onAnswer, void(const std::string& sdp, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD1(onIceCandidate, void(const Candidate& candidate));
       MOCK_METHOD0(detach, void());
       MOCK_METHOD0(hangup, void());
       MOCK_METHOD1(onMediaChanged, void(const std::shared_ptr<Media>& media));
       MOCK_METHOD0(onHangup, void());
       MOCK_METHOD0(onDetach, void());
       MOCK_METHOD1(setDelegate, void(const std::shared_ptr<PluginDelegate>& delegate));
    };

    class CommandMock : public Command {
     public:
       MOCK_METHOD3(action, void(const std::shared_ptr<Signaling>& signaling, const std::shared_ptr<Peer>& peer, const std::string& data));
    };

    class JanusSignalingFactoryMock : public JanusSignalingFactory {
     public:
       MOCK_METHOD3(create, std::shared_ptr<Signaling>(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol));
    };

    class SignalingMock : public Signaling {
     public:
       MOCK_METHOD2(message, void(const std::string& body, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD3(jsep, void(const std::string& body, const std::shared_ptr<Jsep>& jsep, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD1(trickle, void(const Candidate& candidate));
       MOCK_METHOD2(attach, void(const std::string& id, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD0(detach, void());
       MOCK_METHOD0(hangup, void());
    };

    class PeerMock : public Peer {
     public:
       MOCK_METHOD2(createOffer, void(const Constraints& constraints, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD2(createAnswer, void(const Constraints& constraints, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD1(setLocalDescription, void(const std::shared_ptr<Jsep>& jsep));
       MOCK_METHOD1(setRemoteDescription, void(const std::shared_ptr<Jsep>& jsep));
       MOCK_METHOD1(addIceCandidate, void(const Candidate& candidate));
       MOCK_METHOD0(getMedia, std::shared_ptr<Media>());
       MOCK_METHOD0(close, void());
    };

    class EventMock : public JanusEvent {
     public:
       MOCK_METHOD0(jsep, std::shared_ptr<Jsep>());
       MOCK_METHOD0(data, std::string());
    };

    class PluginDelegateMock : public PluginDelegate {
     public:
       MOCK_METHOD2(onEvent, void(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context));
       MOCK_METHOD1(onMediaChanged, void(const std::shared_ptr<Media>& media));
       MOCK_METHOD0(onHangup, void());
       MOCK_METHOD0(onDetach, void());
       MOCK_METHOD1(onOffer, std::string(const std::string& sdp));
       MOCK_METHOD1(onAnswer, std::string(const std::string& sdp));
    };

    class AsyncMock : public Async {
     public:
       MOCK_METHOD0(shutdown, void());
       MOCK_METHOD1(submit, void(const std::shared_ptr<Task>& task));
    };

    class UserMediaMock : public Media {
     public:
       MOCK_METHOD0(localVideoTrack, std::shared_ptr<LocalVideoTrack>());
       MOCK_METHOD0(localAudioTrack, std::shared_ptr<LocalAudioTrack>());
       MOCK_METHOD0(remoteVideoTrack, std::shared_ptr<RemoteVideoTrack>());
       MOCK_METHOD0(remoteAudioTrack, std::shared_ptr<RemoteAudioTrack>());
    };

    class JanusConfMock : public JanusConf {
      public:
        MOCK_METHOD0(url, std::string());
        MOCK_METHOD0(iceServers, std::vector<IceServer>());
        MOCK_METHOD0(webrtc, std::unordered_map<std::string, std::string>());
    };
  }
}
