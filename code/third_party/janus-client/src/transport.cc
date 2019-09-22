#include "janus/transport.h"

namespace Janus {

  Transport::Transport(const std::shared_ptr<TransportDelegate>& delegate) {
    this->_delegate = delegate;
  }

  HttpTransport::HttpTransport(const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<HttpFactory>& httpFactory, const std::shared_ptr<TransportDelegate>& delegate) : Transport(delegate) {
    auto url = baseUrl + "/" + sessionId;

    std::lock_guard<std::mutex> lock(this->mutex);
    for(int index = 0; index < 3; index++) {
      auto client = httpFactory->create(url);
      this->_availableClients.push(client);
      this->_clients.push_back(client);
    }
  }

  void HttpTransport::get() {
    this->_client([&](const std::shared_ptr<Http>& client) {
        auto response = client->get("/");
        // auto evt = std::make_shared<Event>(response.body);
        auto context = ArgBundle::create();
        this->_delegate->onMessage(response.body, context);
    });
  }

  void HttpTransport::request(const std::shared_ptr<Message>& request) {
    this->request(request, ArgBundle::create());
  }

  void HttpTransport::request(const std::shared_ptr<Message>& request, const std::shared_ptr<ArgBundle>& context) {
    this->_client([&](const std::shared_ptr<Http>& client) {
        Request httpRequest(request->serialize(), Query(), Headers());
        auto response = client->post("/", httpRequest);

        // auto msg = std::make_shared<Message>(response.body);
        this->_delegate->onMessage(response.body, context);
    });
  }

  TransportType HttpTransport::type() {
    return TransportType::HTTP;
  }

  void HttpTransport::close() {
    auto iterator = this->_clients.begin();
    while(iterator != this->_clients.end()) {
      iterator->get()->interrupt();
      iterator = this->_clients.erase(iterator);
    }
  }

  HttpTransport::~HttpTransport() {
    this->close();
  }

  void HttpTransport::_client(const std::function<void(const std::shared_ptr<Http>&)>& action) {
    std::unique_lock<std::mutex> getLock(this->mutex);
    this->notEmpty.wait(getLock, [&]{ return this->_availableClients.size() != 0; });
    auto client = this->_availableClients.front();
    this->_availableClients.pop();
    getLock.unlock();
    this->notEmpty.notify_one();

    action(client);

    std::unique_lock<std::mutex> pushLock(this->mutex);
    this->_availableClients.push(client);
    pushLock.unlock();
    this->notEmpty.notify_one();
  }

  std::shared_ptr<Transport> TransportFactoryImpl::create(const TransportType& type, const std::string& baseUrl, const std::string& sessionId, const std::shared_ptr<TransportDelegate>& delegate) {
    auto httpFactory = std::make_shared<HttpFactoryImpl>();

    return std::make_shared<HttpTransport>(baseUrl, sessionId, httpFactory, delegate);
  }

}  // namespace Janus
