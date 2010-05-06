import unittest
from simplejson import JSONDecoder, JSONEncoder
from restful_lib import Connection
from sys import exit

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
C1_onlyp = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] }\
          })
C1_onlyp_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}
C1_onlyt = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_onlyt_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': None, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C1_full = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C1_full2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C2_full2 = JSONEncoder().encode({ '@name': 'C2', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full2_wrongcowner = JSONEncoder().encode({ '@name': 'C1', '@owner': 'xxxx',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full2_wrongpowner1 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'xxxx'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full2_wrongpowner2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'xxxx'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full2_wrongtowner1 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'xxxx'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full2_wrongtowner2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'xxxx'} ] }\
          })
C1_full2_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C2_empty = JSONEncoder().encode({ '@name': 'C2', '@owner': 'testc' })


class DeleteOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C2_empty)

# delete channels using different roles, orders, channel types
    def test_Unauthorized(self):
        response = conn_none.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])
        response = conn_none.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_AuthorizedAsTag(self):
        response = conn_tag.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
        response = conn_tag.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsProp(self):
        response = conn_prop.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
        response = conn_prop.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsChan12(self):
        response = conn_chan.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_chan.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
    def test_AuthorizedAsChan21(self):
        response = conn_chan.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_chan.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
    def test_AuthorizedAsAdmin12(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_admin.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
    def test_AuthorizedAsAdmin21(self):
        response = conn_admin.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


class CreateOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'

# add one "empty" channel (no properties or tags) using different roles
    def test_EmptyUnauthorized(self):
        response = conn_none.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_EmptyAuthorizedAsTag(self):
        response = conn_tag.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_EmptyAuthorizedAsProp(self):
        response = conn_prop.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_EmptyAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_empty_r)
    def test_EmptyAuthorizedAsAdmin(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_empty_r)
# add channel with wrong channel name in payload
    def test_EmptyAuthorizedAsChanWrongChannelName(self):
        response = conn_chan.request_put(self.url2, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_EmptyAuthorizedAsAdminWrongChannelName(self):
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('400', response[u'headers']['status'])
# add one channel (only properties, no tags)
    def test_OnlypAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_onlyp)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyp_r)
    def test_OnlypAuthorizedAsAdmin(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_onlyp)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyp_r)
# add one channel (no properties, only tags)
    def test_OnlytAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_onlyt)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyt_r)
    def test_OnlytAuthorizedAsAdmin(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_onlyt)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyt_r)

# add one "full" channel (with properties and tags)
    def test_FullAuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full_r)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)


class UpdateOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)

# update channel using different roles
    def test_Unauthorized(self):
        response = conn_none.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_AuthorizedAsTag(self):
        response = conn_tag.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsProp(self):
        response = conn_prop.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsChan(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full2_r)
    def test_AuthorizedAsAdmin(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full2_r)
# update channel with wrong channel owner in payload
    def test_AuthorizedAsChanWrongChannelOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongcowner)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongChannelOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongcowner)
        self.failUnlessEqual('400', response[u'headers']['status'])
# add channel with wrong property owner (new property) in payload
    def test_AuthorizedAsChanWrongNewPropertyOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongNewPropertyOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
# add channel with wrong property owner (existing property) in payload
    def test_AuthorizedAsChanWrongExistingPropertyOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongExistingPropertyOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
# add channel with wrong tag owner (new tag) in payload
    def test_AuthorizedAsChanWrongNewTagOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongNewTagOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
# add channel with wrong tag owner (existing tag) in payload
    def test_AuthorizedAsChanWrongExistingTagOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongExistingTagOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)


if __name__ == '__main__':

# Check if database is empty
    response = conn_none.request_get('resources/channels', headers=jsonheader)
    assert '200' == response[u'headers']['status'], 'Database list request returned an error'
    j1 = JSONDecoder().decode(response[u'body'])
    if (None != j1[u'channels']):
        print "Database not empty."
        exit(1)

    unittest.main()
