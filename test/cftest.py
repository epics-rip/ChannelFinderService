import unittest
from simplejson import JSONDecoder, JSONEncoder
from restful_lib import Connection
import sys, os

base_url = os.getenv("BASEURL")
if base_url is None:
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
xmlheader = {'content-type':'application/xml','accept':'application/xml'}

_C1_empty = { '@name': 'C1', '@owner': 'testc' }
C1_empty = JSONEncoder().encode(_C1_empty)
C1_empty_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': None, u'tags': None}
C1_t1_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': None, u'tags':\
          {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}]}}
C1_onlyp = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] }\
          })
C1_onlyp_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}
C1_onlyt = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_onlyt_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': None, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
_C1_full = { '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          }
C1_full = JSONEncoder().encode(_C1_full)
C1_full_xml = "<channel name=\"C1\" owner=\"testc\"><properties><property name=\"P1\" value=\"prop1\" owner=\"testp\"/><property name=\"P2\" value=\"prop2\" owner=\"testp\"/></properties><tags><tag name=\"T1\" owner=\"testt\"/><tag name=\"T2\" owner=\"testt\"/></tags></channel>"
C1_full_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C1s_full_r = {u'channels': {u'channel': C1_full_r}}
_C1_full_lc = { '@name': 'c1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'p1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'p2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 't1', '@owner': 'testt'}, {'@name': 't2', '@owner': 'testt'} ] }\
          }
C1_full_lc = JSONEncoder().encode(_C1_full_lc)
C1_full_lc_r = {u'@owner': u'testc', u'@name': u'c1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'p1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'p2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u't1'}, {u'@owner': u'testt', u'@name': u't2'}]}}

_C1_tx = { '@name': 'C1', '@owner': 'testc', u'tags': {u'tag': {u'@owner': u'testx', u'@name': u'TX'}} }
_C2_tx = { '@name': 'C2', '@owner': 'testc', u'tags': {u'tag': {u'@owner': u'testx', u'@name': u'TX'}} }
_C2_ty = { '@name': 'C2', '@owner': 'testc', u'tags': {u'tag': {u'@owner': u'testy', u'@name': u'TX'}} }
C12_tx = JSONEncoder().encode({'channels': {'channel': [ _C1_tx, _C2_tx ]}})
C12_txy = JSONEncoder().encode({'channels': {'channel': [ _C1_tx, _C2_ty ]}})
_C2_empty = { '@name': 'C2', '@owner': 'testc' }
C2_empty = JSONEncoder().encode(_C2_empty)
_C2_full = { '@name': 'C2', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P22', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T22', '@owner': 'testt'} ] }\
          }
C2_full = JSONEncoder().encode(_C2_full)
C2_full_r = {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}
C2s_full_r = {u'channels': {u'channel': C2_full_r}}
_C3_full = { '@name': 'C3', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          }
C3_full = JSONEncoder().encode(_C3_full)
C3_full_r = {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C3s_full_r = {u'channels': {u'channel': C3_full_r}}
_C4_full = { '@name': 'C4', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P1', '@value': 'prop1', '@owner': 'testp'},\
                                       {'@name': 'P22', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T1', '@owner': 'testt'}, {'@name': 'T22', '@owner': 'testt'} ] }\
          }
C4_full = JSONEncoder().encode(_C4_full)
C4s_full_r = {u'channels': {u'channel': {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}}}
C23_full_r = {u'channels': {u'channel': [C2_full_r, C3_full_r]}}
C1_full2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
C1_full3 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'p2', '@value': 'prop22', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 't2', '@owner': 'testt'} ] }\
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
_C1_full2_wrongpowner1 = { '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'xxxx'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          }
C1_full2_wrongpowner1 = JSONEncoder().encode(_C1_full2_wrongpowner1)
C1_full2_wrongpowner2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'xxxx'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          })
_C1_full2_wrongpowner3 = { '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'xxxx'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          }
C1_full2_wrongpowner3 = JSONEncoder().encode(_C1_full2_wrongpowner3)
_C1_full2_wrongtowner1 = { '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'xxxx'}, {'@name': 'T2', '@owner': 'testt'} ] }\
          }
C1_full2_wrongtowner1 = JSONEncoder().encode(_C1_full2_wrongtowner1)
_C1_full2_wrongtowner3 = { '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'xxxx'} ] }\
          }
C1_full2_wrongtowner3 = JSONEncoder().encode(_C1_full2_wrongtowner3)
C1_full2_wrongtowner2 = JSONEncoder().encode({ '@name': 'C1', '@owner': 'testc',\
          'properties': {'property': [ {'@name': 'P11', '@value': 'prop11', '@owner': 'testp'},\
                                       {'@name': 'P2', '@value': 'prop2', '@owner': 'testp'} ] },\
          'tags':       {'tag':      [ {'@name': 'T11', '@owner': 'testt'}, {'@name': 'T2', '@owner': 'xxxx'} ] }\
          })
C1_full2_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}
C1_full12_r = {u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}

C12_full = JSONEncoder().encode({'channels': {'channel': [ _C1_full, _C2_full ]}})
C121_full = JSONEncoder().encode({'channels': {'channel': [ _C1_full, _C2_full, _C1_full ]}})
C2s_full = JSONEncoder().encode({'channels': {'channel': _C2_full }})
C12_empty = JSONEncoder().encode({'channels': {'channel':[ _C1_empty, _C2_empty ]}})
C34_full = JSONEncoder().encode({'channels': {'channel': [ _C3_full, _C4_full ]}})
C12_full_r = {u'channels': {u'channel': [C1_full_r, C2_full_r]}}
C13_full_r = {u'channels': {u'channel': [C1_full_r, C3_full_r]}}
C12_full_wrongpowner1 = JSONEncoder().encode({'channels': {'channel': [ _C1_full2_wrongpowner1, _C2_full ]}})
C12_full_wrongtowner1 = JSONEncoder().encode({'channels': {'channel': [ _C1_full2_wrongtowner1, _C2_full ]}})
C12_full_wrongpowner3 = JSONEncoder().encode({'channels': {'channel': [ _C1_full2_wrongpowner3, _C2_full ]}})
C12_full_wrongtowner3 = JSONEncoder().encode({'channels': {'channel': [ _C1_full2_wrongtowner3, _C2_full ]}})
C1234_full = JSONEncoder().encode({'channels': {'channel': [ _C1_full, _C2_full, _C3_full, _C4_full ]}})
# 4 channels
C1234_full_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
C1234_t12_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': {u'@owner': u'testt', u'@name': u'T22'}}}]}}
C12_t12_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
# 4 channels, T1 being at C3, C4
C1234_t34_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': {u'@owner': u'testt', u'@name': u'T2'}}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
C34_t34_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
# 4 channels, T1 being at C1, C2, C4
C1234_t124_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
C124_t124_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
# 4 channels, TX being at C1, C2
C1234_tx_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}, {u'@owner': u'testx', u'@name': u'TX'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}, {u'@owner': u'testx', u'@name': u'TX'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
C12_tx_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T1'}, {u'@owner': u'testt', u'@name': u'T2'}, {u'@owner': u'testx', u'@name': u'TX'}]}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}, {u'@owner': u'testx', u'@name': u'TX'}]}}]}}
# 4 channels, no T1
C1234_nt1_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': {u'@owner': u'testt', u'@name': u'T2'}}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}, {u'@owner': u'testc', u'@name': u'C3', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T2'}]}}, {u'@owner': u'testc', u'@name': u'C4', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': {u'@owner': u'testt', u'@name': u'T22'}}}]}}
# 2 channels, no T1
C12_nt1_r = {u'channels': {u'channel': [{u'@owner': u'testc', u'@name': u'C1', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': u'testp', u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': {u'@owner': u'testt', u'@name': u'T2'}}}, {u'@owner': u'testc', u'@name': u'C2', u'properties': {u'property': [{u'@owner': u'testp', u'@name': u'P11', u'@value': u'prop11'}, {u'@owner': u'testp', u'@name': u'P22', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': u'testt', u'@name': u'T11'}, {u'@owner': u'testt', u'@name': u'T22'}]}}]}}
None_r = {u'channels': None}
# One tag only
_T1 = {u'@owner': u'testt', u'@name': u'T1'}
T1 = JSONEncoder().encode(_T1)
T1_xml = "<tag name=\"T1\" owner=\"testt\"/>"
_T1_wrongowner = {u'@owner': u'xxxx', u'@name': u'T1'}
T1_wrongowner = JSONEncoder().encode(_T1_wrongowner)
_TX = {u'@owner': u'testt', u'@name': u'TX'}
TX = JSONEncoder().encode(_TX)
_TX_wrongowner = {u'@owner': u'xxxx', u'@name': u'TX'}
TX_wrongowner = JSONEncoder().encode(_TX_wrongowner)


#############################################################################################
# Test .../channel/{name} DELETE
#############################################################################################
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

    def doTestAndCheck12(self, conn):
        response = conn.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
    def test_AuthorizedAsChan12(self):
        self.doTestAndCheck12(conn_chan)
    def test_AuthorizedAsAdmin12(self):
        self.doTestAndCheck12(conn_admin)

    def doTestAndCheck21(self, conn):
        response = conn.request_delete(self.url2, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url2, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn.request_delete(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
    def test_AuthorizedAsChan21(self):
        self.doTestAndCheck21(conn_chan)
    def test_AuthorizedAsAdmin21(self):
        self.doTestAndCheck21(conn_admin)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


#############################################################################################
# Test .../channel/{name} PUT           with new channel
#############################################################################################
class CreateOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url1_lc = 'resources/channel/c1'
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

    def doTestAndCheckEmpty(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_empty_r)
    def test_EmptyAuthorizedAsChan(self):
        self.doTestAndCheckEmpty(conn_chan)
    def test_EmptyAuthorizedAsAdmin(self):
        self.doTestAndCheckEmpty(conn_admin)

# add channel with wrong channel name in payload
    def test_EmptyAuthorizedAsChanWrongChannelName(self):
        response = conn_chan.request_put(self.url2, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified channel name C2 and payload channel name C1 do not match") == -1)
    def test_EmptyAuthorizedAsAdminWrongChannelName(self):
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C1_empty)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified channel name C2 and payload channel name C1 do not match") == -1)

# add one channel (only properties, no tags)
    def doTestAndCheckOnlyProp(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_onlyp)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyp_r)
    def test_OnlyPropAuthorizedAsChan(self):
        self.doTestAndCheckOnlyProp(conn_chan)
    def test_OnlyPropAuthorizedAsAdmin(self):
        self.doTestAndCheckOnlyProp(conn_admin)

# add one channel (no properties, only tags)
    def doTestAndCheckOnlyTag(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_onlyt)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_onlyt_r)
    def test_OnlyTagAuthorizedAsChan(self):
        self.doTestAndCheckOnlyTag(conn_chan)
    def test_OnlyTagAuthorizedAsAdmin(self):
        self.doTestAndCheckOnlyTag(conn_admin)

# add one "full" channel (with properties and tags)
    def doTestAndCheckFull(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full_r)
    def test_FullAuthorizedAsChan(self):
        self.doTestAndCheckFull(conn_chan)
    def test_FullAuthorizedAsAdmin(self):
        self.doTestAndCheckFull(conn_admin)

# add one "full" channel (with properties and tags) lowercase names
    def doTestAndCheckFullLowercase(self, conn):
        response = conn.request_put(self.url1_lc, headers=jsonheader, body=C1_full_lc)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full_lc_r)
    def test_FullAuthorizedAsChanLowercase(self):
        self.doTestAndCheckFullLowercase(conn_chan)
    def test_FullAuthorizedAsAdminLowercase(self):
        self.doTestAndCheckFullLowercase(conn_admin)

# add one "full" channel (with properties and tags) as XML
    def doTestAndCheckFullXml(self, conn):
        response = conn.request_put(self.url1, headers=xmlheader, body=C1_full_xml)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full_r)
    def test_FullAuthorizedAsChanXml(self):
        self.doTestAndCheckFullXml(conn_chan)
    def test_FullAuthorizedAsAdminXml(self):
        self.doTestAndCheckFullXml(conn_admin)

# add properties with wrong payload format (channels instead of channel)
    def test_AuthorizedAsChanWrongFormat(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongFormat(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url1_lc, headers=jsonheader)


#############################################################################################
# Test .../channel/{name} PUT           to existing channel
#############################################################################################
class UpdateOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)

# update channel using different roles and different wrong payload owners
    def test_Unauthorized(self):
        response = conn_none.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_AuthorizedAsTag(self):
        response = conn_tag.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsProp(self):
        response = conn_prop.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('403', response[u'headers']['status'])

    def doTestAndCheck(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full2_r)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# test if property and tag name capitalization to database version works
    def doTestAndCheckPropNameCapitalization(self, conn):
        response = conn.request_put(self.url1, headers=jsonheader, body=C1_full3)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full2_r)
    def test_AuthorizedAsChanPropNameCapitalization(self):
        self.doTestAndCheckPropNameCapitalization(conn_chan)
    def test_AuthorizedAsAdminPropNameCapitalization(self):
        self.doTestAndCheckPropNameCapitalization(conn_admin)

# update channel with wrong channel owner in payload
    def test_AuthorizedAsChanWrongChannelOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongcowner)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for channel C1 do not match") == -1)
    def test_AuthorizedAsAdminWrongChannelOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongcowner)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for channel C1 do not match") == -1)

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
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P2 do not match") == -1)
    def test_AuthorizedAsAdminWrongExistingPropertyOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P2 do not match") == -1)

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
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T2 do not match") == -1)
    def test_AuthorizedAsAdminWrongExistingTagOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T2 do not match") == -1)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)


#############################################################################################
# Test .../channel/{name} PUT           to existing channel - restrictions by other channel
#############################################################################################
class UpdateSecondChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C2_full)

# add channel with wrong property owner (other channel's property) in payload
    def test_AuthorizedAsChanWrongNewPropertyOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P11 do not match") == -1)
    def test_AuthorizedAsAdminWrongNewPropertyOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P11 do not match") == -1)

# add channel with wrong tag owner (other channel's tag) in payload
    def test_AuthorizedAsChanWrongNewTagOwner(self):
        response = conn_chan.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T11 do not match") == -1)
    def test_AuthorizedAsAdminWrongNewTagOwner(self):
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T11 do not match") == -1)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


#############################################################################################
# Test .../channel/{name} POST           to existing channel
#############################################################################################
class UpdatePropertiesOneChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)

# update channel using different roles and different wrong payload owners
    def test_Unauthorized(self):
        response = conn_none.request_post(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_AuthorizedAsTag(self):
        response = conn_tag.request_post(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('403', response[u'headers']['status'])

    def doTestAndCheck(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full12_r)
    def test_AuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# test if property and tag name capitalization to database version works
    def doTestAndCheckPropNameCapitalization(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full3)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1_full12_r)
    def test_AuthorizedAsPropPropNameCapitalization(self):
        self.doTestAndCheck(conn_prop)
    def test_AuthorizedAsAdminPropNameCapitalization(self):
        self.doTestAndCheck(conn_admin)

# add properties with wrong channel name in payload
    def doTestAndCheckWrongChannelName(self, conn):
        response = conn.request_post(self.url2, headers=jsonheader, body=C1_full2)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified channel name C2 and payload channel name C1 do not match") == -1)
    def test_EmptyAuthorizedAsPropWrongChannelName(self):
        self.doTestAndCheckWrongChannelName(conn_prop)
    def test_EmptyAuthorizedAsAdminWrongChannelName(self):
        self.doTestAndCheckWrongChannelName(conn_admin)

# update channel with wrong channel owner in payload
    def doTestAndCheckWrongChannelOwner(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongcowner)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for channel C1 do not match") == -1)
    def test_AuthorizedAsPropWrongChannelOwner(self):
        self.doTestAndCheckWrongChannelOwner(conn_prop)
    def test_AuthorizedAsAdminWrongChannelOwner(self):
        self.doTestAndCheckWrongChannelOwner(conn_admin)

# add properties with wrong property owner (new property) in payload
    def test_AuthorizedAsPropWrongNewPropertyOwner(self):
        response = conn_prop.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongNewPropertyOwner(self):
        response = conn_admin.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])

# add properties with wrong property owner (existing property) in payload
    def doTestAndCheckWrongExistingPropertyOwner(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongpowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P2 do not match") == -1)
    def test_AuthorizedAsPropWrongExistingPropertyOwner(self):
        self.doTestAndCheckWrongExistingPropertyOwner(conn_prop)
    def test_AuthorizedAsAdminWrongExistingPropertyOwner(self):
        self.doTestAndCheckWrongExistingPropertyOwner(conn_admin)

# add properties with wrong tag owner (new tag) in payload
    def test_AuthorizedAsPropWrongNewTagOwner(self):
        response = conn_prop.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongNewTagOwner(self):
        response = conn_admin.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('204', response[u'headers']['status'])

# add properties with wrong tag owner (existing tag) in payload
    def doTestAndCheckWrongExistingTagOwner(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongtowner2)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T2 do not match") == -1)
    def test_AuthorizedAsPropWrongExistingTagOwner(self):
        self.doTestAndCheckWrongExistingTagOwner(conn_prop)
    def test_AuthorizedAsAdminWrongExistingTagOwner(self):
        self.doTestAndCheckWrongExistingTagOwner(conn_admin)

# add properties with wrong payload format (channels instead of channel)
    def test_AuthorizedAsPropWrongFormat(self):
        response = conn_prop.request_post(self.url1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongFormat(self):
        response = conn_admin.request_post(self.url1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])

# add properties for nonexisting channel
    def test_AuthorizedAsPropNewChannel(self):
        response = conn_prop.request_post(self.url2, headers=jsonheader, body=C2_full)
        self.failUnlessEqual('403', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified channel C2 does not exist") == -1)
    def test_AuthorizedAsAdminNewChannel(self):
        response = conn_admin.request_post(self.url2, headers=jsonheader, body=C2_full)
        self.failUnlessEqual('403', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified channel C2 does not exist") == -1)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


#############################################################################################
# Test .../channel/{name} POST           to existing channel - restrictions by other channel
#############################################################################################
class UpdatePropertiesSecondChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        response = conn_admin.request_put(self.url1, headers=jsonheader, body=C1_full)
        response = conn_admin.request_put(self.url2, headers=jsonheader, body=C2_full)

# add properties with wrong property owner (other channel's property) in payload
    def doTestAndCheckWrongNewPropertyOwner(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongpowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P11 do not match") == -1)
    def test_AuthorizedAsPropWrongNewPropertyOwner(self):
        self.doTestAndCheckWrongNewPropertyOwner(conn_prop)
    def test_AuthorizedAsAdminWrongNewPropertyOwner(self):
        self.doTestAndCheckWrongNewPropertyOwner(conn_admin)

# add properties with wrong tag owner (other channel's tag) in payload
    def doTestAndCheckWrongNewPropertyOwner(self, conn):
        response = conn.request_post(self.url1, headers=jsonheader, body=C1_full2_wrongtowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T11 do not match") == -1)
    def test_AuthorizedAsPropWrongNewTagOwner(self):
        self.doTestAndCheckWrongNewPropertyOwner(conn_prop)
    def test_AuthorizedAsAdminWrongNewTagOwner(self):
        self.doTestAndCheckWrongNewPropertyOwner(conn_admin)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


#############################################################################################
# Test .../channels POST
#############################################################################################
class createManyChannels(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        self.url = 'resources/channels'

# add two channels using different roles
    def test_Unauthorized(self):
        response = conn_none.request_post(self.url, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('401', response[u'headers']['status'])
    def test_AuthorizedAsTag(self):
        response = conn_tag.request_post(self.url, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('403', response[u'headers']['status'])
    def test_AuthorizedAsProp(self):
        response = conn_prop.request_post(self.url, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('403', response[u'headers']['status'])

    def doTestAndCheck(self, conn):
        response = conn.request_post(self.url, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.url, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_full_r)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# using wrong payload format (channel instead of channels)
    def test_AuthorizedAsChanWrongFormat(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongFormat(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])

# multiple channels of the same name in the payload
    def test_AuthorizedAsChanMultipleSameChannel(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C121_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Payload contains multiple instances of channel C1") == -1)
    def test_AuthorizedAsAdminMultipleSameChannel(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C121_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Payload contains multiple instances of channel C1") == -1)

# using inconsistent property ownership in the payload
    def test_AuthorizedAsChanInconsistentPropertyOwner(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C12_full_wrongpowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Inconsistent payload owner for property P11") == -1)
    def test_AuthorizedAsAdminInconsistentPropertyOwner(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C12_full_wrongpowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Inconsistent payload owner for property P11") == -1)

# using inconsistent tag ownership in the payload
    def test_AuthorizedAsChanInconsistentTagOwner(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C12_full_wrongtowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Inconsistent payload owner for tag T11") == -1)
    def test_AuthorizedAsAdminInconsistentTagOwner(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C12_full_wrongtowner1)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Inconsistent payload owner for tag T11") == -1)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)


#############################################################################################
# Test .../channels POST                              restrictions by other (third) channel
#############################################################################################
class createManyChannelsThirdChannel(unittest.TestCase):
    def setUp(self):
        self.url1 = 'resources/channel/C1'
        self.url2 = 'resources/channel/C2'
        self.url3 = 'resources/channel/C3'
        self.url = 'resources/channels'
        response = conn_admin.request_put(self.url3, headers=jsonheader, body=C3_full)

# add properties with wrong property owner (other channel's property) in payload
    def test_AuthorizedAsChanWrongNewPropertyOwner(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C12_full_wrongpowner3)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P2 do not match") == -1)
    def test_AuthorizedAsAdminWrongNewPropertyOwner(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C12_full_wrongpowner3)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag P2 do not match") == -1)

# add properties with wrong tag owner (other channel's tag) in payload
    def test_AuthorizedAsChanWrongNewTagOwner(self):
        response = conn_chan.request_post(self.url, headers=jsonheader, body=C12_full_wrongtowner3)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T2 do not match") == -1)
    def test_AuthorizedAsAdminWrongNewTagOwner(self):
        response = conn_admin.request_post(self.url, headers=jsonheader, body=C12_full_wrongtowner3)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Database and payload owner for property/tag T2 do not match") == -1)

    def tearDown(self):
        response = conn_admin.request_delete(self.url1, headers=jsonheader)
        response = conn_admin.request_delete(self.url2, headers=jsonheader)
        response = conn_admin.request_delete(self.url3, headers=jsonheader)


#############################################################################################
# Test .../channels GET                             all kinds of queries
#############################################################################################
class queryChannels(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c3 = 'resources/channel/C3'
        self.c4 = 'resources/channel/C4'
        self.c  = 'resources/channels'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C1234_full)

    def test_AllChans(self):
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_full_r)

    def test_OnePropValue(self):
        response = conn_none.request_get(self.c + "?P11=prop1", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def test_TwoDifferentPropValues(self):
        response = conn_none.request_get(self.c + "?P1=prop1&P22=prop22", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C4s_full_r)

    def test_TwoPropValues(self):
        response = conn_none.request_get(self.c + "?P11=prop1&P11=prop11", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C23_full_r)

    def test_AndOrCombiPropValues(self):
        response = conn_none.request_get(self.c + "?P11=prop1&P11=prop11&P2=prop2", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def test_OneTagValue(self):
        response = conn_none.request_get(self.c + "?~tag=t2", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C13_full_r)

    def test_TwoTagValues(self):
        response = conn_none.request_get(self.c + "?~tag=t2&~tag=T1", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1s_full_r)

    def test_TwoTagValuesNoResult(self):
        response = conn_none.request_get(self.c + "?~tag=T2&~tag=t3", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, None_r)

    def test_TagAndPropValues(self):
        response = conn_none.request_get(self.c + "?p11=prop1&P11=prop11&~tag=t2", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def test_TagAndPropPattern1(self):
        response = conn_none.request_get(self.c + "?p11=prop*&~tag=t2", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def test_TagAndPropPattern2(self):
        response = conn_none.request_get(self.c + "?p11=prop*&~tag=t?", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def test_TagAndPropPattern3(self):
        response = conn_none.request_get(self.c + "?p1=prop*&~tag=t?&~tag=t??", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C4s_full_r)

    def test_ChannelAndPropValues(self):
        response = conn_none.request_get(self.c + "?p11=prop1&P11=prop11&~name=c3", headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C3s_full_r)

    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)
        response = conn_admin.request_delete(self.c3, headers=jsonheader)
        response = conn_admin.request_delete(self.c4, headers=jsonheader)


#############################################################################################
# Test .../tags/<name> PUT
#############################################################################################
class addTagExclusiveToChannel(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c3 = 'resources/channel/C3'
        self.c4 = 'resources/channel/C4'
        self.c = 'resources/channels'
        self.t1 = 'resources/tags/T1'
        self.t1_ = 'resources/tags/t1'
        self.tx = 'resources/tags/TX'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C1234_full)

# add tag to channels [1,2] then [3,4], using different roles
# always checking that the complete channel list and the tags/T1 GET return the correct channels/tags 
    def test_Unauthorized(self):
        response = conn_none.request_put(self.t1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('401', response[u'headers']['status'])

    def doTestAndCheck(self, conn, url):
        response = conn.request_put(url, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_t12_r)
        response = conn_none.request_get(url, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_t12_r)
        response = conn.request_put(url, headers=jsonheader, body=C34_full)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_t34_r)
        response = conn_none.request_get(url, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C34_t34_r)
    def test_AuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag, self.t1)
    def test_AuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop, self.t1)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan, self.t1)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin, self.t1)

# tag name capitalization (of URL) to database version
    def test_AuthorizedAsTagDbSpelling(self):
        self.doTestAndCheck(conn_tag, self.t1_)
    def test_AuthorizedAsAdminDbSpelling(self):
        self.doTestAndCheck(conn_admin, self.t1_)

# add new tag to channels 1,2 (specifying owner in payload)
    def doTestAndCheck12X(self, conn):
        response = conn.request_put(self.tx, headers=jsonheader, body=C12_tx)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_tx_r)
        response = conn_none.request_get(self.tx, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_tx_r)
    def test_AuthorizedAsTagNewTagSpecOwner(self):
        self.doTestAndCheck12X(conn_tag)
    def test_AuthorizedAsAdminNewTagSpecOwner(self):
        self.doTestAndCheck12X(conn_admin)

# using unspecified owner for new tag
    def doTestAndCheckNewTagUnspecifiedOwner(self, conn):
        response = conn.request_put(self.tx, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Tag ownership for TX undefined in db and payload") == -1)
    def test_AuthorizedAsTagNewTagUnspecifiedOwner(self):
        self.doTestAndCheckNewTagUnspecifiedOwner(conn_tag)
    def test_AuthorizedAsAdminNewTagUnspecifiedOwner(self):
        self.doTestAndCheckNewTagUnspecifiedOwner(conn_admin)

# multiple channels of the same name in the payload
    def doTestAndCheckMultiSameChannel(self, conn):
        response = conn.request_put(self.t1, headers=jsonheader, body=C121_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Payload contains multiple instances of channel C1") == -1)
    def test_AuthorizedAsTagMultipleSameChannel(self):
        self.doTestAndCheckMultiSameChannel(conn_tag)
    def test_AuthorizedAsAdminMultipleSameChannel(self):
        self.doTestAndCheckMultiSameChannel(conn_admin)

# using wrong payload format (channel instead of channels)
    def test_AuthorizedAsTagWrongFormat(self):
        response = conn_tag.request_put(self.t1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongFormat(self):
        response = conn_admin.request_put(self.t1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)
        response = conn_admin.request_delete(self.c3, headers=jsonheader)
        response = conn_admin.request_delete(self.c4, headers=jsonheader)


#############################################################################################
# Test .../tags/<name> DELETE
#############################################################################################
class deleteTag(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c3 = 'resources/channel/C3'
        self.c4 = 'resources/channel/C4'
        self.c = 'resources/channels'
        self.t1 = 'resources/tags/T1'
        self.tx = 'resources/tags/TX'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C1234_full)

    def test_Unauthorized(self):
        response = conn_none.request_delete(self.t1, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])

    def doTestAndCheck(self, conn):
        response = conn.request_delete(self.t1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_nt1_r)
        response = conn_none.request_get(self.t1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, None_r)
    def test_AuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag)
    def test_AuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# delete nonexisting tag
    def doTestAndCheckNonexistingTag(self, conn):
        response = conn.request_delete(self.tx, headers=jsonheader)
        self.failUnlessEqual('404', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Tag TX does not exist") == -1)
    def test_AuthorizedAsTagNonexistingTag(self):
        self.doTestAndCheckNonexistingTag(conn_tag)
    def test_AuthorizedAsAdminNonexistingTag(self):
        self.doTestAndCheckNonexistingTag(conn_admin)


    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)
        response = conn_admin.request_delete(self.c3, headers=jsonheader)
        response = conn_admin.request_delete(self.c4, headers=jsonheader)


#############################################################################################
# Test .../tags/<name> POST
#############################################################################################
class addTagToChannel(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c3 = 'resources/channel/C3'
        self.c4 = 'resources/channel/C4'
        self.c = 'resources/channels'
        self.t1 = 'resources/tags/T1'
        self.tx = 'resources/tags/TX'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C1234_full)

    def test_Unauthorized(self):
        response = conn_none.request_post(self.t1, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('401', response[u'headers']['status'])

# always checking that the complete channel list and the tags/T1 GET return the correct channels/tags 
    def doTestAndCheck(self, conn, payload):
        response = conn.request_post(self.t1, headers=jsonheader, body=payload)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_t124_r)
        response = conn_none.request_get(self.t1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C124_t124_r)
# add tag to channels 1,2 (using full [1,2] payload), using different roles
    def test_FullAuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag, C12_full)
    def test_FullAuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop, C12_full)
    def test_FullAuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan, C12_full)
    def test_FullAuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin, C12_full)
# add tag to channels 1,2 (using empty [1,2] payload), using different roles
    def test_EmptyAuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag, C12_empty)
    def test_EmptyAuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin, C12_empty)

# add tag to channels 1,2 (specifying owner in payload), using different roles
    def doTestAndCheck12X(self, conn):
        response = conn.request_post(self.tx, headers=jsonheader, body=C12_tx)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C1234_tx_r)
        response = conn_none.request_get(self.tx, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_tx_r)
    def test_AuthorizedAsTagNewTagSpecOwner(self):
        self.doTestAndCheck12X(conn_tag)
    def test_AuthorizedAsAdminNewTagSpecOwner(self):
        self.doTestAndCheck12X(conn_admin)

# using unspecified owner for new tag
    def doTestAndCheckNewTagUnspecifiedOwner(self, conn):
        response = conn.request_post(self.tx, headers=jsonheader, body=C12_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Tag ownership for TX undefined in db and payload") == -1)
    def test_AuthorizedAsTagNewTagUnspecifiedOwner(self):
        self.doTestAndCheckNewTagUnspecifiedOwner(conn_tag)
    def test_AuthorizedAsAdminNewTagUnspecifiedOwner(self):
        self.doTestAndCheckNewTagUnspecifiedOwner(conn_admin)

# using inconsistent owner for new tag
    def doTestAndCheckNewTagInconsistentOwner(self, conn):
        response = conn.request_post(self.tx, headers=jsonheader, body=C12_txy)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Inconsistent payload owner for tag TX") == -1)
    def test_AuthorizedAsTagNewTagInconsistentOwner(self):
        self.doTestAndCheckNewTagInconsistentOwner(conn_tag)
    def test_AuthorizedAsAdminNewTagInconsistentOwner(self):
        self.doTestAndCheckNewTagInconsistentOwner(conn_admin)

# using wrong payload format (channel instead of channels)
    def test_AuthorizedAsTagWrongFormat(self):
        response = conn_tag.request_post(self.t1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsAdminWrongFormat(self):
        response = conn_admin.request_post(self.t1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])

    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)
        response = conn_admin.request_delete(self.c3, headers=jsonheader)
        response = conn_admin.request_delete(self.c4, headers=jsonheader)


#############################################################################################
# Test .../tags/<name>/<channel> PUT
#############################################################################################
class addTagToOneChannel(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c = 'resources/channels'
        self.t1 = 'resources/tags/T1/C2'
        self.tx = 'resources/tags/TX/C2'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C12_full)

# add tag to channel 2 using different roles
    def test_Unauthorized(self):
        response = conn_none.request_put(self.t1, headers=jsonheader, body=T1)
        self.failUnlessEqual('401', response[u'headers']['status'])

# Add one tag
    def doTestAndCheck(self, conn):
        response = conn.request_put(self.t1, headers=jsonheader, body=T1)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_t12_r)
    def test_AuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag)
    def test_AuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# Same as XML
    def doTestAndCheckXml(self, conn):
        response = conn.request_put(self.t1, headers=xmlheader, body=T1_xml)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_t12_r)
    def test_AuthorizedAsTagXml(self):
        self.doTestAndCheckXml(conn_tag)
    def test_AuthorizedAsAdminXml(self):
        self.doTestAndCheckXml(conn_admin)

# no payload (as tag already exists)
    def doTestAndCheckNoPayload(self, conn):
        response = conn.request_put(self.t1, headers=jsonheader)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_t12_r)
    def test_AuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

# using wrong payload format (channel instead of tag)
    def doTestAndCheckWrongFormat(self, conn):
        response = conn.request_put(self.t1, headers=jsonheader, body=C1_full)
        self.failUnlessEqual('400', response[u'headers']['status'])
    def test_AuthorizedAsTagWrongFormat(self):
        self.doTestAndCheckWrongFormat(conn_tag)
    def test_AuthorizedAsAdminWrongFormat(self):
        self.doTestAndCheckWrongFormat(conn_admin)

# using wrong payload format (channel instead of tag) as XML
    def doTestAndCheckWrongFormatXml(self, conn):
        response = conn.request_put(self.t1, headers=xmlheader, body=C1_full_xml)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("syntactically incorrect") == -1)
    def test_AuthorizedAsTagWrongFormatXml(self):
        self.doTestAndCheckWrongFormatXml(conn_tag)
    def test_AuthorizedAsAdminWrongFormatXml(self):
        self.doTestAndCheckWrongFormatXml(conn_admin)

# using wrong name in payload (TX instead of T1)
    def doTestAndCheckWrongTagName(self, conn):
        response = conn.request_put(self.t1, headers=jsonheader, body=TX)
        self.failUnlessEqual('400', response[u'headers']['status'])
        self.failIf(response[u'body'].find("Specified tag name T1 and payload tag name TX do not match") == -1)
    def test_AuthorizedAsTagWrongTagName(self):
        self.doTestAndCheckWrongTagName(conn_tag)
    def test_AuthorizedAsAdminWrongTagName(self):
        self.doTestAndCheckWrongTagName(conn_admin)

    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)


#############################################################################################
# Test .../tags/<name>/<channel> DELETE
#############################################################################################
class deleteTagFromOneChannel(unittest.TestCase):
    def setUp(self):
        self.c1 = 'resources/channel/C1'
        self.c2 = 'resources/channel/C2'
        self.c = 'resources/channels'
        self.t1 = 'resources/tags/T1/C1'
        response = conn_admin.request_post(self.c, headers=jsonheader, body=C12_full)

# Delete tag from channel 1 using different roles
    def test_Unauthorized(self):
        response = conn_none.request_delete(self.t1, headers=jsonheader)
        self.failUnlessEqual('401', response[u'headers']['status'])

# Delete tag
    def doTestAndCheck(self, conn):
        response = conn.request_delete(self.t1, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        response = conn_none.request_get(self.c, headers=jsonheader)
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, C12_nt1_r)
    def test_AuthorizedAsTag(self):
        self.doTestAndCheck(conn_tag)
    def test_AuthorizedAsProp(self):
        self.doTestAndCheck(conn_prop)
    def test_AuthorizedAsChan(self):
        self.doTestAndCheck(conn_chan)
    def test_AuthorizedAsAdmin(self):
        self.doTestAndCheck(conn_admin)

    def tearDown(self):
        response = conn_admin.request_delete(self.c1, headers=jsonheader)
        response = conn_admin.request_delete(self.c2, headers=jsonheader)


if __name__ == '__main__':

# Check if database is empty
    response = conn_none.request_get('resources/channels', headers=jsonheader)
    assert '200' == response[u'headers']['status'], 'Database list request returned an error'
    j1 = JSONDecoder().decode(response[u'body'])
    if (None != j1[u'channels']):
        print "Database not empty."
        sys.exit(1)

    unittest.main()
