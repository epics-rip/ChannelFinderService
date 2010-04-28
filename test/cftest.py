import unittest
from simplejson import JSONDecoder, JSONEncoder
from restful_lib import Connection

base_url = "http://localhost:8080/ChannelFinder"


user_tag = "taggy"
user_prop = "proppy"
user_chan = "channy"
user_admin = "boss"
passwd = "1234"

conn_none = Connection(base_url)
conn_tag  = Connection(base_url, username=user_tag, password=passwd)
conn_prop = Connection(base_url, username=user_prop, password=passwd)
conn_chan = Connection(base_url, username=user_chan, password=passwd)
conn_admin = Connection(base_url, username=user_admin, password=passwd)

jsonheader = {'content-type':'application/json','accept':'application/json'}

C1_empty = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc' })
C1_empty_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': None, u'tags': None}
C1_full = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C2_empty = JSONEncoder().encode({ '@name': 'C2', '@owner': 'testc' })

class CreateOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'

# add one "empty" channel (no properties or tags) using different roles
    def test_addChannelUnauthorized(self):
        response = conn_none.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_addChannelAuthorizedAsTag(self):
        response = conn_tag.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_addChannelAuthorizedAsProp(self):
        response = conn_prop.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_addEmptyChannelAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_empty_r)
    def test_addEmptyChannelAuthorizedAsAdmin(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_empty_r)
# add channel with wrong name in payload
    def test_addEmptyChannelAuthorizedAsChanWrongName(self):
        response = conn_chan.request_put(self.url2, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('500', response[u'headers']['status'])
# add one "full" channel (with properties and tags)
    def test_addFullChannelAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full_r)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)


class DeleteOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C2_empty)

# delete channels using different roles
    def test_deleteChannelUnauthorized(self):
        response = conn_none.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])
        response = conn_none.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_deleteChannelAuthorizedAsTag(self):
        response = conn_tag.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
        response = conn_tag.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_deleteChannelAuthorizedAsProp(self):
        response = conn_prop.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
        response = conn_prop.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_deleteChannelAuthorizedAsChan(self):
        response = conn_chan.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_chan.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
    def test_deleteChannelAuthorizedAsAdmin(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_admin.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


if __name__ == '__main__':

# Check if database is empty
    response = conn_none.request_get('resources/channels', headers=jsonheader)
    assert '200' == response[u'headers']['status'], 'Database list request returned an error'
    j1 = JSONDecoder().decode(response[u'body'])
    assert None == j1[u'channels'], 'Database not empty'

    unittest.main()
