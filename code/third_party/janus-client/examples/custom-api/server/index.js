const express = require('express');
const uuid = require('uuid/v4');
const pkg = require('./package');
const WebSocket = require('ws');
const EventEmitter = require('events');
const { json } = require('body-parser');

const janusHost = process.env.JANUS_HOST || 'ws://janus:8188';

const app = express();
const port = 3000;

app.use(json());

const _sessions = {};

const _connector = function(evts) {
  return new Promise(function(resolve, reject) {
    const session = new WebSocket(janusHost, 'janus-protocol');
    const _internalSend = session.send;
    session.send = function(body) {
      if(session._session != null) {
        body.session_id = session._session;
      }

      if(session._handle != null) {
        body.handle_id = session._handle;
      }

      _internalSend.call(session, JSON.stringify(body));
    };

    session.on('open', function() {
      const _handlers = new EventEmitter();

      session.on('message', function(data) {
        data = JSON.parse(data);

        _handlers.emit(data.transaction, data);
      });

      session.on('close', function() {
        evts.onClose();
      });

      const _proxy = {};

      _proxy.call = function(offer) {
        return new Promise(function(resolve, reject) {
          const transaction = uuid();

          const handler = function(reply) {
            if(reply.janus === 'success' && reply.session_id == null) {
              session._session = reply.data.id;

              session.send({
                janus: 'attach',
                plugin: 'janus.plugin.echotest',
                transaction
              });
            } else if(reply.janus === 'success' && reply.session_id != null) {
              session._handle = reply.data.id;
              _handlers.once(transaction, handler);

              session.send({
                janus: 'message',
                transaction,
                body: {
                  audio: true,
                  video: true
                },
                jsep: offer
              });
            } else if(reply.janus === 'event' && reply.jsep != null) {
              resolve(reply.jsep);

              return;
            }

            _handlers.once(transaction, handler);
          };

          _handlers.once(transaction, handler);

          session.send({
            janus: 'create',
            transaction
          });
        });
      };

      _proxy.trickle = function(candidate) {
        session.send({
          janus: 'trickle',
          transaction: uuid(),
          candidate
        });
      };

      _proxy.hangup = function() {
        session.close();
      };

      resolve(_proxy);
    });
  });
};

app.get('/', function(req, res) {
  res.send(`${ pkg.name } v${ pkg.version }`)
});

app.post('/call', function(req, res) {
  const id = uuid();

  _connector({
    onClose: function() {
      delete _sessions[id];
    }
  }).then(function(session) {
    _sessions[id] = session;

    return session.call(req.body);
  }).then(function(jsep) {
    res.json({
      jsep,
      id
    });
  });
});

app.post('/trickle/:id', function(req, res) {
  _sessions[req.params.id].trickle(req.body);

  res.end();
});

app.post('/hangup/:id', function(req, res) {
  _sessions[req.params.id].hangup();

  res.end();
});

app.listen(port, function() {
  console.log(`Custom api listening on port ${port}!`);
});
