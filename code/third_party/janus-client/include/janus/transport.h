#pragma once

#include <string>
#include <vector>
#include <functional>
#include <queue>
#include <memory>
#include <condition_variable>
#include "janus/messages.h"
#include "janus/arg_bundle_impl.h"
#include "janus/http.h"
#include "janus/transport_type.hpp"
#include "janus/transport_delegate.hpp"

namespace Janus {

  class Transport {
   public:
     explicit Transport(const std::shared_ptr<TransportDelegate>& delegate);

     virtual void get() = 0;
     virtual void request(const std::shared_ptr<Message>& request) = 0;
     virtual void request(const std::shared_ptr<Message>& request, const std::shared_ptr<ArgBundle>& context) = 0;
     virtual TransportType type() = 0;
     virtual void close() = 0;

   protected:
     std::shared_ptr<TransportDelegate> _delegate = nullptr;
  };

  class HttpTransport : public Transport {
   public:
     HttpTransport(const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<HttpFactory>& httpFactory, const std::shared_ptr<TransportDelegate>& delegate);
     ~HttpTransport();

     void get();
     void request(const std::shared_ptr<Message>& request);
     void request(const std::shared_ptr<Message>& request, const std::shared_ptr<ArgBundle>& context);
     TransportType type();
     void close();

   private:
     std::vector<std::shared_ptr<Http>> _clients;
     std::queue<std::shared_ptr<Http>> _availableClients;

     // pool implementation
     void _client(const std::function<void(const std::shared_ptr<Http>&)>& action);
     std::mutex mutex;
     std::condition_variable notEmpty;
  };

  class TransportFactory {
   public:
     virtual std::shared_ptr<Transport> create(const TransportType& type, const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<TransportDelegate>& delegate) = 0;
  };

  class TransportFactoryImpl : public TransportFactory {
   public:
     std::shared_ptr<Transport> create(const TransportType& type, const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<TransportDelegate>& delegate);
  };

}  // namespace Janus
