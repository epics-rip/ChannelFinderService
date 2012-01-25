import unittest
try: from json import JSONDecoder, JSONEncoder
except ImportError: from simplejson import JSONDecoder, JSONEncoder
from restful_lib import Connection
from copy import copy
import sys, os

from _testConf import _testConf

base_url = os.getenv("BASEURL")
if base_url is None:
    base_url = _testConf.get('DEFAULT', 'BaseURL')

user_tag = _testConf.get('DEFAULT', 'tagUsername')
user_prop = _testConf.get('DEFAULT', 'propUsername')
user_prop2 =_testConf.get('DEFAULT', 'propUsername2')
user_chan = _testConf.get('DEFAULT', 'channelUsername')
user_chan2 =_testConf.get('DEFAULT', 'channelUsername2')
user_admin = _testConf.get('DEFAULT', 'username')

conn_none  = Connection(base_url)
conn_tag   = Connection(base_url, username=user_tag,   \
                        password=_testConf.get('DEFAULT', 'tagPassword'))
conn_prop  = Connection(base_url, username=user_prop,  \
                        password=_testConf.get('DEFAULT', 'propPassword'))
conn_prop2 = Connection(base_url, username=user_prop2, \
                        password=_testConf.get('DEFAULT', 'propPassword2'))
conn_chan  = Connection(base_url, username=user_chan,  \
                        password=_testConf.get('DEFAULT', 'channelPassword'))
conn_chan2 = Connection(base_url, username=user_chan2, \
                        password=_testConf.get('DEFAULT', 'channelPassword2'))
conn_admin = Connection(base_url, username=user_admin, \
                        password=_testConf.get('DEFAULT', 'password'))

jsonheader = {'content-type':'application/json','accept':'application/json'}
xmlheader = {'content-type':'application/xml','accept':'application/xml'}

testc=_testConf.get('DEFAULT', 'channelOwner')
testp=_testConf.get('DEFAULT', 'propOwner')
testt=_testConf.get('DEFAULT', 'tagOwner')

#     Channels
C1_empty = '{"@name": "C1", "@owner": "' + testc + '"}'
C1_empty_r = {u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': None}
c1_empty = '{"@name": "c1", "@owner": "' + testc + '"}'
C1t_empty = '{"@name": "C1", "@owner": "' + testt + '"}'
C1nn_empty = '{"@owner": "' + testc + '"}'
C1no_empty = '{"@name": "C1"}'
C1en_empty = '{"@name": "", "@owner": "' + testc + '"}'
C1eo_empty = '{"@name": "C1", "@owner": ""}'
C2_empty = '{"@name": "C2", "@owner": "' + testc + '"}'
C3_empty = '{"@name": "C3", "@owner": "' + testc + '"}'

C1_full = '{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P1", "@value": "prop1"}, {"@name": "P2", "@value": "prop2"}]}, "tags": {"tag": [{"@name": "T1"}, {"@name": "T2"}]}}'
C2_full = '{"@name": "C2", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P3", "@value": "prop3"}, {"@name": "P4", "@value": "prop4"}]}, "tags": {"tag": [{"@name": "T33"}, {"@name": "T44"}]}}'
C3_full = '{"@name": "C3", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P3", "@value": "prop1"}, {"@name": "P2", "@value": "prop2"}]}, "tags": {"tag": [{"@name": "T33"}, {"@name": "T2"}]}}'
C4_full = '{"@name": "C4", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P1", "@value": "prop1"}, {"@name": "P4", "@value": "prop4"}]}, "tags": {"tag": [{"@name": "T1"}, {"@name": "T44"}]}}'

C1_full2 = '{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P1", "@value": "prop11"}, {"@name": "P2", "@value": "prop22"}]}, "tags": {"tag": [{"@name": "T1"}, {"@name": "T44"}]}}'
C1_full2_r = {u'@owner': testc, u'@name': u'C1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop11'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop22'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}, {u'@owner': testt, u'@name': u'T44'}]}}

c1_full_r = {u'@owner': testc, u'@name': u'c1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}
C1t_full_r = {u'@owner': testt, u'@name': u'C1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}

Cs1234_full = '{"channels": {"channel": [' + C1_full + ', ' + C2_full + ', '+ C3_full + ', '+ C4_full + ']}}'
Cs1_full = '{"channels": {"channel": [' + C1_full + ']}}'
Cs121_full = '{"channels": {"channel": [' + C1_full + ', ' + C2_full + ', '+ C1_full + ']}}'
Cs12_1nn_empty = '{"channels": {"channel": [' + C1nn_empty + ', ' + C2_empty + ']}}'
Cs12_1no_empty = '{"channels": {"channel": [' + C1no_empty + ', ' + C2_empty + ']}}'
Cs12_1en_empty = '{"channels": {"channel": [' + C1en_empty + ', ' + C2_empty + ']}}'
Cs12_1eo_empty = '{"channels": {"channel": [' + C1eo_empty + ', ' + C2_empty + ']}}'
C1_full_r = {u'@owner': testc, u'@name': u'C1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}
C2_full_r = {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P3', u'@value': u'prop3'}, {u'@owner': testp, u'@name': u'P4', u'@value': u'prop4'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T33'}, {u'@owner': testt, u'@name': u'T44'}]}}
C3_full_r = {u'@owner': testc, u'@name': u'C3', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}, {u'@owner': testp, u'@name': u'P3', u'@value': u'prop1'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T2'}, {u'@owner': testt, u'@name': u'T33'}]}}
C4_full_r = {u'@owner': testc, u'@name': u'C4', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P4', u'@value': u'prop4'}]}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T44'}]}}
C1eP1_full_r = {u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}}, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}
Cs1234_full_r = {u'channels': {u'channel': [C1_full_r, C2_full_r, C3_full_r, C4_full_r]}}
Cs12_1t_2_empty = '{"channels": {"channel": [' + C1t_empty + ', ' + C2_empty + ']}}'
Cs12_full = '{"channels": {"channel": [' + C1_full + ', ' + C2_full + ']}}'
Cs12_full_r = {u'channels': {u'channel': [C1_full_r, C2_full_r]}}
Cs13_full_r = {u'channels': {u'channel': [C1_full_r, C3_full_r]}}
Cs23_full_r = {u'channels': {u'channel': [C2_full_r, C3_full_r]}}
Cs1_full_r = {u'channels': {u'channel': C1_full_r}}
Cs3_full_r = {u'channels': {u'channel': C3_full_r}}
Cs4_full_r = {u'channels': {u'channel': C4_full_r}}
None_r = {u'channels': None}

# tests with empty, null and no value for property
P1ev = '{"@name": "P1", "@value": ""}'
P1nv = '{"@name": "P1"}'
P3ev = '{"@name": "P3", "@value": ""}'

C1_P1ev = '{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": [' + P1ev + ']}}'
C1_P1nv = '{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": [' + P1nv + ']}}'
C1_P3ev = '{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": [' + P3ev + ']}}'

Cs12_P1ev = '{"channels": {"channel": [' + C1_P1ev + ', ' + C2_full + ']}}'
Cs12_P1nv = '{"channels": {"channel": [' + C1_P1nv + ', ' + C2_full + ']}}'

# replies needed for tags and properties URLs
Cs12_1e2t2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T2'}}}]}}
Cs12_1e2p2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'tags': None, u'properties': {u'property': {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}}}]}}
Cs12_1T1p_2T12p_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': {u'tag': {u'@owner': testp, u'@name': u'T1'}}}, {u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': [{u'@owner': testp, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}]}}
Cs12_1t1_2t1T2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u't1'}}}, {u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': [{u'@owner': testt, u'@name': u't1'}, {u'@owner': testt, u'@name': u'T2'}]}}]}}
Cs123_1T1_2T12_3T2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T1'}}}, {u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}, {u'@owner': testc, u'@name': u'C3', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T2'}}}]}}
Cs12_1P1t_2P12t_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testt, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testt, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}]}}
Cs12_1p1_2p1P2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testp, u'@name': u'p1', u'@value': u'prop1'}}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'p1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}]}}
Cs123_1P1_2P12_3P2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}, {u'@owner': testc, u'@name': u'C3', u'properties': {u'property': {u'@owner': testp, u'@name': u'P2', u'@value': u'prop3'}}, u'tags': None}]}}
Cs123_1P12v_2P12_3e_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'propv'}]}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}, {u'@owner': testc, u'@name': u'C3', u'properties': None, u'tags': None}]}}
Cs12_1T1_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T1'}}}, {u'@owner': testc, u'@name': u'C2', u'tags': None, u'properties': None}]}}
Cs12_1e2T1_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T1'}}}]}}
Cs12_1P1_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'tags': None, u'properties': None}]}}
Cs12_1e2P1_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': None, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': {u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}]}}
Cs2_P12_r = {u'channels': {u'channel': {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}}}

#     Properties
P1_empty = '{"@name": "P1", "@owner": "' + testp + '"}'
P1_empty_r = {u'@owner': testp, u'@name': u'P1'}
p1_empty = '{"@name": "p1", "@owner": "' + testp + '"}'
P1t_empty = '{"@name": "P1", "@owner": "' + testt + '"}'
P1t_empty_r = {u'@owner': testt, u'@name': u'P1'}
P2_empty = '{"@name": "P2", "@owner": "' + testp + '"}'
P2t_empty = '{"@name": "P2", "@owner": "' + testt + '"}'
P3_empty = '{"@name": "P3", "@owner": "' + testp + '"}'
P4_empty = '{"@name": "P4", "@owner": "' + testp + '"}'

Ps1t_empty = '{"properties": { "property": ' + P1t_empty + '}}'
Ps12t_empty = '{"properties": { "property": [' + P1t_empty + ', ' + P2t_empty + ']}}'
Ps12_empty_xml = "<properties><property name=\"P1\" owner=\"testp\"/><property name=\"P2\" owner=\"testp\"/></properties>"
Ps12_empty = '{"properties": { "property": [' + P1_empty + ', ' + P2_empty + ']}}'
Ps12_empty_r = {u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1'}, {u'@owner': testp, u'@name': u'P2'}]}}
Ps12t_empty_r = {u'properties': {u'property': [{u'@owner': testt, u'@name': u'P1'}, {u'@owner': testt, u'@name': u'P2'}]}}
Ps1234_empty = '{"properties": { "property": [' + P1_empty + ', ' + P2_empty + ', ' + P3_empty+ ', ' + P4_empty + ']}}'

P1_C1 = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C1", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1", "@value": "prop1"}}}}}'
P1_C1_r = {u'channels': {u'channel': {u'@owner': testc, u'@name': u'C1', u'properties': {u'property': {u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}}, u'@owner': testp, u'@name': u'P1'}
P1_C1_P1nv = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C1", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1"}}}}}'
P1_C1_P1ev = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C1", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1", "@value": ""}}}}}'
p1_C1 = '{"@name": "p1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C1", "@owner": "' + testc + '", "properties": {"property": {"@name": "p1", "@value": "prop1"}}}}}'
P1_C2 = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C2", "@owner": "' + testc + '", "properties": {"property": [{"@name": "P1", "@value": "prop1"}, {"@name": "P2", "@value": "prop2"}]}}}}'
P1_C2_r = {u'channels': {u'channel': {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': {u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}}, u'tags': None}}, u'@owner': testp, u'@name': u'P1'}
P1_C12 = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : [{"@name": "C1", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1", "@value": "prop1"}}}, {"@name": "C2", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1", "@value": "prop1"}}}]}}'
P1_C3 = '{"@name": "P1", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C3", "@owner": "' + testc + '", "properties": {"property": {"@name": "P1", "@value": "prop1"}}}}}'
P2_C2 = '{"@name": "P2", "@owner": "' + testp + '", "channels": {"channel" : {"@name": "C2", "@owner": "' + testc + '", "properties": {"property": {"@name": "P2", "@value": "prop2"}}}}}'
P2_C1 = '{"@name": "P2", "channels": {"channel" : {"@name": "C1", "properties": {"property": {"@name": "P2", "@value": "prop2"}}}}}'
P2_C23 = '{"@name": "P2", "@owner": "' + testp + '", "channels": {"channel" : [{"@name": "C2", "properties": {"property": {"@name": "P2", "@value": "prop2"}}}, {"@name": "C3", "properties": {"property": {"@name": "P2", "@value": "prop3"}}}]}}'
P2_C23_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}, {u'@owner': testc, u'@name': u'C3', u'properties': {u'property': {u'@owner': testp, u'@name': u'P2', u'@value': u'prop3'}}, u'tags': None}]}, u'@owner': testp, u'@name': u'P2'}
P2_C4 = '{"@name": "P2", "channels": {"channel" : {"@name": "C4", "properties": {"property": {"@name": "P2", "@value": "prop2"}}}}}'
P3_C1 = '{"@name": "P3", "channels": {"channel" : {"@name": "C1", "properties": {"property": {"@name": "P3", "@value": "prop3"}}}}}'
P2_C1v = '{"@name": "P2", "channels": {"channel" : {"@name": "C1", "properties": {"property": {"@name": "P2", "@value": "propv"}}}}}'
P2_C1v2_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C1', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'propv'}]}, u'tags': None}, {u'@owner': testc, u'@name': u'C2', u'properties': {u'property': [{u'@owner': testp, u'@name': u'P1', u'@value': u'prop1'}, {u'@owner': testp, u'@name': u'P2', u'@value': u'prop2'}]}, u'tags': None}]}, u'@owner': testp, u'@name': u'P2'}

#     Tags
T1_empty = '{"@name": "T1", "@owner": "' + testt + '"}'
T1_empty_r = {u'@owner': testt, u'@name': u'T1'}
t1_empty = '{"@name": "t1", "@owner": "' + testt + '"}'
T1p_empty = '{"@name": "T1", "@owner": "' + testp + '"}'
T1p_empty_r = {u'@owner': testp, u'@name': u'T1'}
T2_empty = '{"@name": "T2", "@owner": "' + testt + '"}'
T2p_empty = '{"@name": "T2", "@owner": "' + testp + '"}'
T3_empty = '{"@name": "T33", "@owner": "' + testt + '"}'
T4_empty = '{"@name": "T44", "@owner": "' + testt + '"}'

Ts1p_empty = '{"tags": { "tag": ' + T1p_empty + '}}'
Ts12p_empty = '{"tags": { "tag": [' + T1p_empty + ', ' + T2p_empty + ']}}'
Ts12_empty_xml = "<tags><tag name=\"T1\" owner=\"testt\"/><tag name=\"T2\" owner=\"testt\"/></tags>"
Ts12_empty = '{"tags": { "tag": [' + T1_empty + ', ' + T2_empty + ']}}'
Ts12_empty_r = {u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}
Ts12p_empty_r = {u'tags': {u'tag': [{u'@owner': testp, u'@name': u'T1'}, {u'@owner': testp, u'@name': u'T2'}]}}
Ts1234_empty = '{"tags": { "tag": [' + T1_empty + ', ' + T2_empty + ', ' + T3_empty + ', ' + T4_empty + ']}}'

T1_C1 = '{"@name": "T1", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C1", "tags": {"tag": {"@name": "T1"}}}}}'
T1_C1_r = {u'channels': {u'channel': {u'@owner': testc, u'@name': u'C1', u'tags': {u'tag': {u'@owner': testt, u'@name': u'T1'}}, u'properties': None}}, u'@owner': testt, u'@name': u'T1'}
t1_C1 = '{"@name": "t1", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C1", "@owner": "' + testc + '", "tags": {"tag": {"@name": "t1"}}}}}'
T1_C2 = '{"@name": "T1", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C2", "@owner": "' + testc + '", "tags": {"tag": {"@name": "T1"}}}}}'
T1_C2_r = {u'channels': {u'channel': {u'@owner': testc, u'@name': u'C2', u'tags': {u'tag': {u'@owner': testt, u'@name': u'T1'}}, u'properties': None}}, u'@owner': testt, u'@name': u'T1'}
T1_C12 = '{"@name": "T1", "@owner": "' + testt + '", "channels": {"channel" : [{"@name": "C1", "@owner": "' + testc + '", "tags": {"tag": {"@name": "T1"}}}, {"@name": "C2", "@owner": "' + testc + '", "tags": {"tag": {"@name": "T1"}}}]}}'
T1_C3 = '{"@name": "T1", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C3", "tags": {"tag": {"@name": "T1"}}}}}'
T2_C2 = '{"@name": "T2", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C2", "@owner": "' + testc + '", "tags": {"tag": {"@name": "T2"}}}}}'
T2_C23 = '{"@name": "T2", "@owner": "' + testt + '", "channels": {"channel" : [{"@name": "C2", "tags": {"tag": {"@name": "T2"}}}, {"@name": "C3", "tags": {"tag": {"@name": "T2"}}}]}}'
T2_C23_r = {u'channels': {u'channel': [{u'@owner': testc, u'@name': u'C2', u'properties': None, u'tags': {u'tag': [{u'@owner': testt, u'@name': u'T1'}, {u'@owner': testt, u'@name': u'T2'}]}}, {u'@owner': testc, u'@name': u'C3', u'properties': None, u'tags': {u'tag': {u'@owner': testt, u'@name': u'T2'}}}]}, u'@owner': testt, u'@name': u'T2'}
t2_C23 = '{"@name": "t2", "@owner": "' + testt + '", "channels": {"channel" : [{"@name": "C2", "tags": {"tag": {"@name": "t2"}}}, {"@name": "C3", "tags": {"tag": {"@name": "t2"}}}]}}'
T3_C1 = '{"@name": "T33", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C1", "tags": {"tag": {"@name": "T33"}}}}}'
T2_C4 = '{"@name": "T2", "@owner": "' + testt + '", "channels": {"channel" : {"@name": "C4", "tags": {"tag": {"@name": "T2"}}}}}'


#############################################################################################
# Generic action and check functions
#############################################################################################

def doGetJSON(self, conn, g_url, g_result, g_resp):
    response = conn_none.request_get(g_url, headers=copy(jsonheader))
    self.assertEqual(`g_resp`, response[u'headers']['status'],
    'unexpected return code for get operation - expected ' + `g_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    if (response[u'headers']['status'] != '404'):
        j = JSONDecoder().decode(response[u'body'])
        self.assertEqual(j, g_result,
        'unexpected result of get operation - expected:\n' + `g_result` + '\nreceived:\n' + `j`)

def doPostAndGetJSON(self, conn, p_url, p_body, p_resp, g_url, g_result, g_resp):
    response = conn.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    doGetJSON(self, conn, g_url, g_result, g_resp)

def doPostAndFailJSON(self, conn, p_url, p_body, p_resp):
    response = conn.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])

def doPostAndFailMessageJSON(self, conn, p_url, p_body, p_resp, err_mess):
    response = conn.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    self.assertFalse(response[u'body'].find(err_mess) == -1,
    'error message from server:\n' + response[u'body'] + '\ndoes not contain the expected string: "' + err_mess + '"')

def doPutAndGetJSON(self, conn, p_url, p_body, p_resp, g_url, g_result, g_resp):
    response = conn.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for put operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    doGetJSON(self, conn, g_url, g_result, g_resp)

def doPutAndFailJSON(self, conn, p_url, p_body, p_resp):
    response = conn.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for put operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])

def doPutAndFailMessageJSON(self, conn, p_url, p_body, p_resp, err_mess):
    response = conn.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for put operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    self.assertFalse(response[u'body'].find(err_mess) == -1,
    'error message from server:\n' + response[u'body'] + '\ndoes not contain the expected string: "' + err_mess + '"')

def doDeleteAndGetJSON(self, conn, d_url, d_resp, g_url, g_result, g_resp):
    response = conn.request_delete(d_url, headers=copy(jsonheader))
    self.assertEqual(`d_resp`, response[u'headers']['status'],
    'unexpected return code for delete operation - expected ' + `d_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    doGetJSON(self, conn, g_url, g_result, g_resp)

def doDeleteAndFailJSON(self, conn, d_url, d_resp):
    response = conn.request_delete(d_url, headers=copy(jsonheader))
    self.assertEqual(`d_resp`, response[u'headers']['status'],
    'unexpected return code for delete operation - expected ' + `d_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])

def doDeleteAndFailMessageJSON(self, conn, d_url, d_resp, err_mess):
    response = conn.request_delete(d_url, headers=copy(jsonheader))
    self.assertEqual(`d_resp`, response[u'headers']['status'],
    'unexpected return code for delete operation - expected ' + `d_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    self.assertFalse(response[u'body'].find(err_mess) == -1,
    'error message from server:\n' + response[u'body'] + '\ndoes not contain the expected string: "' + err_mess + '"')


#############################################################################################
# Test .../channels GET                             all kinds of queries
#############################################################################################
class QueryChannels(unittest.TestCase):
    def setUp(self):
        self.p  = 'resources/properties'
        self.t  = 'resources/tags'
        self.c  = 'resources/channels'
        response = conn_admin.request_post(self.t, headers=copy(jsonheader), body=Ts1234_empty)
        response = conn_admin.request_post(self.p, headers=copy(jsonheader), body=Ps1234_empty)
        response = conn_admin.request_post(self.c, headers=copy(jsonheader), body=Cs1234_full)

    def test_AllChans(self):
        doGetJSON(self, conn_none, self.c + "", Cs1234_full_r, 200)

    def test_OneNameStarPattern(self):
        doGetJSON(self, conn_none, self.c + "?~name=C*", Cs1234_full_r, 200)

    def test_OneNameQuestMPattern(self):
        doGetJSON(self, conn_none, self.c + "?~name=C?", Cs1234_full_r, 200)

    def test_TwoNameValues(self):
        doGetJSON(self, conn_none, self.c + "?~name=C1&~name=C2", Cs12_full_r, 200)

    def test_OnePropValue(self):
        doGetJSON(self, conn_none, self.c + "?P3=prop1", Cs3_full_r, 200)

    def test_TwoDifferentPropValues(self):
        doGetJSON(self, conn_none, self.c + "?P1=prop1&P4=prop4", Cs4_full_r, 200)

    def test_TwoPropValues(self):
        doGetJSON(self, conn_none, self.c + "?P3=prop1&P3=prop3", Cs23_full_r, 200)

    def test_AndOrCombiPropValues(self):
        doGetJSON(self, conn_none, self.c + "?P3=prop1&P3=prop3&P2=prop2", Cs3_full_r, 200)

    def test_OneTag(self):
        doGetJSON(self, conn_none, self.c + "?~tag=t2", Cs13_full_r, 200)

    def test_OneProperty(self):
        doGetJSON(self, conn_none, self.c + "?~tag=p3", Cs23_full_r, 200)

    def test_TwoTags(self):
        doGetJSON(self, conn_none, self.c + "?~tag=t2&~tag=T1", Cs1_full_r, 200)

    def test_TwoTagValuesNoResult(self):
        doGetJSON(self, conn_none, self.c + "?~tag=T2&~tag=t5", None_r, 200)

    def test_TagAndPropValues(self):
        doGetJSON(self, conn_none, self.c + "?p3=prop1&P3=prop3&~tag=t2", Cs3_full_r, 200)

    def test_TagAndPropPattern1(self):
        doGetJSON(self, conn_none, self.c + "?p3=prop*&~tag=t2", Cs3_full_r, 200)

    def test_TagAndPropPattern2(self):
        doGetJSON(self, conn_none, self.c + "?p3=prop*&~tag=t?", Cs3_full_r, 200)

    def test_TagAndPropPattern3(self):
        doGetJSON(self, conn_none, self.c + "?p1=prop*&~tag=t?&~tag=t??", Cs4_full_r, 200)

    def test_ChannelAndPropValues(self):
        doGetJSON(self, conn_none, self.c + "?p3=prop1&P3=prop3&~name=c3", Cs3_full_r, 200)

    def tearDown(self):
        response = conn_admin.request_delete(self.c + '/C1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.c + '/C2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.c + '/C3', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.c + '/C4', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P3', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P4', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T33', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T44', headers=copy(jsonheader))


#############################################################################################
# Test .../channels PUT                          not implemented
#############################################################################################
class PutManyChannels(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.c = 'resources/channels'

    def test_AuthorizedAsAdmin(self):
        doPutAndFailJSON(self, conn_admin, self.c, Cs1_full, 405)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))


#############################################################################################
# Test .../channels POST
#############################################################################################
class PostManyChannels(unittest.TestCase):
    def setUp(self):
        self.t  = 'resources/tags'
        self.p  = 'resources/properties'
        self.c  = 'resources/channels'
        response = conn_admin.request_post(self.t, headers=copy(jsonheader), body=Ts1234_empty)
        response = conn_admin.request_post(self.p, headers=copy(jsonheader), body=Ps1234_empty)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.c, Cs12_full, 401)
    def test_AuthorizedAsTag(self):
        doPostAndFailJSON(self, conn_tag, self.c, Cs12_full, 403)
    def test_AuthorizedAsProp(self):
        doPostAndFailJSON(self, conn_prop, self.c, Cs12_full, 403)

    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.c, Cs12_full, 204, self.c, Cs12_full_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.c, Cs12_full, 204, self.c, Cs12_full_r, 200)

# same as channy2 (not member of testt)
    def test_AuthorizedAsChanny2(self):
        doPostAndFailMessageJSON(self, conn_chan2, self.c, Cs12_1t_2_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of channel 'C1'")

# channel with invalid payload (no name and/or owner)
    def test_AuthorizedAsChanNoName(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_1nn_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsAdminNoName(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_1nn_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsChanNoOwner(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminNoOwner(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanEmptyName(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_1en_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsAdminEmptyName(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_1en_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsChanEmptyOwner(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminEmptyOwner(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanEmptyPropValue(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminEmptyPropValue(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsChanNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_chan, self.c, Cs12_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_admin, self.c, Cs12_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.c + '/C1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.c + '/C2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P3', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P4', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T33', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T44', headers=copy(jsonheader))


#############################################################################################
# Test .../channel/{name} PUT           with new channel
#############################################################################################
class PutOneChannel(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.p = 'resources/properties'
        self.t = 'resources/tags'
        response = conn_admin.request_post(self.t, headers=copy(jsonheader), body=Ts12_empty)
        response = conn_admin.request_post(self.p, headers=copy(jsonheader), body=Ps12_empty)

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.C1, C1_full, 401)
    def test_AuthorizedAsTag(self):
        doPutAndFailJSON(self, conn_tag, self.C1, C1_full, 403)
    def test_AuthorizedAsProp(self):
        doPutAndFailJSON(self, conn_prop, self.C1, C1_full, 403)

# add one "empty" channel (no properties and tags)
    def test_EmptyAuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)
    def test_EmptyAuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)

# add one "empty" channel (no properties and tags) onto a full channel
    def test_EmptyOnFullAuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndGetJSON(self, conn_chan, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)
    def test_EmptyOnFullAuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndGetJSON(self, conn_admin, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)

# add one "full" channel (with properties and tags)
    def test_FullAuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
    def test_FullAuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.C1, C1_full, 204, self.C1, C1_full_r, 200)

# add one "full" channel (with properties and tags) onto an empty channel
    def test_FullOnEmptyAuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
    def test_FullOnEmptyAuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.C1, C1_empty, 204, self.C1, C1_empty_r, 200)
        doPutAndGetJSON(self, conn_admin, self.C1, C1_full, 204, self.C1, C1_full_r, 200)

# Payload and URL names do not match
    def test_EmptyAuthorizedAsChanLcPayload(self):
        doPutAndFailMessageJSON(self, conn_chan, self.C1, c1_empty, 400, "Specified channel name 'C1' and payload channel name 'c1' do not match")

# same as channy2 (not member of testt)
    def test_AuthorizedAsChanny2(self):
        doPutAndFailMessageJSON(self, conn_chan2, self.C1, C1t_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of channel 'C1'")

# channel with invalid payload (no name and/or owner)
    def test_AuthorizedAsChanNoName(self):
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1nn_empty, 400, "Invalid channel name")
    def test_AuthorizedAsAdminNoName(self):
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1nn_empty, 400, "Invalid channel name")
    def test_AuthorizedAsChanNoOwner(self):
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminNoOwner(self):
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanEmptyName(self):
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1en_empty, 400, "Invalid channel name")
    def test_AuthorizedAsAdminEmptyName(self):
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1en_empty, 400, "Invalid channel name")
    def test_AuthorizedAsChanEmptyOwner(self):
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminEmptyOwner(self):
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanEmptyPropValue(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminEmptyPropValue(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsChanNullPropValue(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndFailMessageJSON(self, conn_chan, self.C1, C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPutAndGetJSON(self, conn_chan, self.C1, C1_full, 204, self.C1, C1_full_r, 200)
        doPutAndFailMessageJSON(self, conn_admin, self.C1, C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T2', headers=copy(jsonheader))


#############################################################################################
# Test .../channel/{name} POST and GET
#############################################################################################
class PostOneChannel(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.p = 'resources/properties'
        self.t = 'resources/tags'
        response = conn_admin.request_post(self.t, headers=copy(jsonheader), body=Ts1234_empty)
        response = conn_admin.request_post(self.p, headers=copy(jsonheader), body=Ps12_empty)
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_full)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.C1, C1_full2, 401)
    def test_AuthorizedAsTag(self):
        doPostAndFailJSON(self, conn_tag, self.C1, C1_full2, 403)
    def test_AuthorizedAsProp(self):
        doPostAndFailJSON(self, conn_prop, self.C1, C1_full2, 403)

    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.C1, C1_full2, 204, self.C1, C1_full2_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.C1, C1_full2, 204, self.C1, C1_full2_r, 200)

# Set a new channel name
    def test_AuthorizedAsChanNewName(self):
        doPostAndGetJSON(self, conn_chan, self.C1, c1_empty, 204, self.C1, c1_full_r, 200)
    def test_AuthorizedAsAdminNewName(self):
        doPostAndGetJSON(self, conn_admin, self.C1, c1_empty, 204, self.C1, c1_full_r, 200)

# Set a new channel owner
    def test_AuthorizedAsChanNewOwner(self):
        doPostAndGetJSON(self, conn_chan, self.C1, C1t_empty, 204, self.C1, C1t_full_r, 200)
    def test_AuthorizedAsAdminNewOwner(self):
        doPostAndGetJSON(self, conn_admin, self.C1, C1t_empty, 204, self.C1, C1t_full_r, 200)

# New channel owner (non member of new owner)
    def test_AuthorizedAsChanNewOwnerNonMemberNew(self):
        doPostAndFailMessageJSON(self, conn_chan2, self.C1, C1t_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of channel 'C1'")

# New channel owner (non member of old owner)
    def test_AuthorizedAsChanNewOwnerNonMemberOld(self):
        doPostAndGetJSON(self, conn_chan, self.C1, C1t_empty, 204, self.C1, C1t_full_r, 200)
        doPostAndFailMessageJSON(self, conn_chan2, self.C1, C1_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of channel 'C1'")

# Delete existing property P1
    def test_AuthorizedAsChanDeleteProperty(self):
        doPostAndGetJSON(self, conn_chan, self.C1, C1_P1ev, 204, self.C1, C1eP1_full_r, 200)

# Delete existing property P1
    def test_AuthorizedAsChanDeleteNonexistingProperty(self):
        doPostAndFailJSON(self, conn_chan, self.C1, C1_P3ev, 404)

# channel with invalid payload (no name and/or owner)
    def test_AuthorizedAsChanNoName(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C1, C1nn_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsAdminNoName(self):
        doPostAndFailMessageJSON(self, conn_admin, self.C1, C1nn_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsChanNoOwner(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C1, C1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminNoOwner(self):
        doPostAndFailMessageJSON(self, conn_admin, self.C1, C1no_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanEmptyName(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C1, C1en_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsAdminEmptyName(self):
        doPostAndFailMessageJSON(self, conn_admin, self.C1, C1en_empty, 400, "Invalid channel name ")
    def test_AuthorizedAsChanEmptyOwner(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C1, C1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsAdminEmptyOwner(self):
        doPostAndFailMessageJSON(self, conn_admin, self.C1, C1eo_empty, 400, "Invalid channel owner (null or empty string) for 'C1'")
    def test_AuthorizedAsChanNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C1, C1_P1nv, 400, "Invalid property value (missing or null) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_admin, self.C1, C1_P1nv, 400, "Invalid property value (missing or null) for 'P1'")

# post for nonexisting channel
    def test_AuthorizedAsChanNonexChannel(self):
        doPostAndFailMessageJSON(self, conn_chan, self.C2, C2_full, 404, "Specified channel 'C2' does not exist")

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p + '/P2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T1', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T2', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T33', headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t + '/T44', headers=copy(jsonheader))


#############################################################################################
# Test .../channel/{name} DELETE
#############################################################################################
class DeleteChannel(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.C3 = 'resources/channels/C3'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.P2 = 'resources/properties/P2'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1t_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_C12)
        response = conn_admin.request_put(self.P2, headers=copy(jsonheader), body=P2_C2)

    def test_Unauthorized(self):
        doDeleteAndFailJSON(self, conn_none, self.C1, 401)
    def test_AuthorizedAsTag(self):
        doDeleteAndFailJSON(self, conn_tag, self.C1, 403)
    def test_AuthorizedAsProp(self):
        doDeleteAndFailJSON(self, conn_prop, self.C1, 403)

# Delete C1 and check on .../channels/C1 and .../channels URLs
    def test_AuthorizedAsChan(self):
        doDeleteAndGetJSON(self, conn_chan, self.C1, 200, self.C1, "", 404)
        doGetJSON(self, conn_chan, self.c, Cs2_P12_r, 200)
    def test_AuthorizedAsAdmin(self):
        doDeleteAndGetJSON(self, conn_admin, self.C1, 200, self.C1, "", 404)
        doGetJSON(self, conn_admin, self.c, Cs2_P12_r, 200)

# Delete both C1 and C2, check on both .../channels/C<n> and .../channels URLs
    def test_AuthorizedAsChanBoth(self):
        doDeleteAndGetJSON(self, conn_chan, self.C1, 200, self.C1, "", 404)
        doDeleteAndGetJSON(self, conn_chan, self.C2, 200, self.C2, "", 404)
        doGetJSON(self, conn_chan, self.c, None_r, 200)
    def test_AuthorizedAsAdminBoth(self):
        doDeleteAndGetJSON(self, conn_admin, self.C1, 200, self.C1, "", 404)
        doDeleteAndGetJSON(self, conn_admin, self.C2, 200, self.C2, "", 404)
        doGetJSON(self, conn_admin, self.c, None_r, 200)

# delete as channy2 (does not belong to testt)
    def test_AuthorizedAsChanny2(self):
        doDeleteAndFailMessageJSON(self, conn_chan2, self.C1, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of channel 'C1'")

# delete nonexisting channel
    def test_AuthorizedAsChanNonexistingChannel(self):
        doDeleteAndFailJSON(self, conn_chan, self.C3, 404)
    def test_AuthorizedAsAdminNonexistingChannel(self):
        doDeleteAndFailJSON(self, conn_chan, self.C3, 404)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C3, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P2, headers=copy(jsonheader))


#############################################################################################
# Test .../tags PUT                          not implemented
#############################################################################################
class PutManyTags(unittest.TestCase):
    def setUp(self):
        self.T1 = 'resources/tags/T1'
        self.t = 'resources/tags'

    def test_AuthorizedAsAdmin(self):
        doPutAndFailJSON(self, conn_admin, self.t, Ts1p_empty, 405)

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))


#############################################################################################
# Test .../tags POST and GET
#############################################################################################
class PostManyTags(unittest.TestCase):
    """Test POST and GET on the .../tags target"""
    def setUp(self):
        self.t = 'resources/tags'
        self.T1 = 'resources/tags/T1'
        self.T2 = 'resources/tags/T2'

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.t, Ts12_empty, 401)

    def test_AuthorizedAsTag(self):
        doPostAndGetJSON(self, conn_tag, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)
    def test_AuthorizedAsProp(self):
        doPostAndGetJSON(self, conn_prop, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)
    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)

    def test_AuthorizedAsPropOverwrite(self):
        doPostAndGetJSON(self, conn_prop, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)
        doPostAndGetJSON(self, conn_prop, self.t, Ts12p_empty, 204, self.t, Ts12p_empty_r, 200)
    def test_AuthorizedAsAdminOverwrite(self):
        doPostAndGetJSON(self, conn_admin, self.t, Ts12_empty, 204, self.t, Ts12_empty_r, 200)
        doPostAndGetJSON(self, conn_admin, self.t, Ts12p_empty, 204, self.t, Ts12p_empty_r, 200)

# As '" + user_chan2 + "' user that does not belong to group of tag
    def test_AuthorizedAsChanny2(self):
        doPostAndFailMessageJSON(self, conn_chan2, self.t, Ts12_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T2, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name> PUT and GET   simple version, no channels
#############################################################################################
class PutOneTag(unittest.TestCase):
    """Test PUT and GET on the .../tags/<name> target - without channels in the payload"""
    def setUp(self):
        self.T1 = 'resources/tags/T1'

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.T1, T1_empty, 401)

    def test_AuthorizedAsTag(self):
        doPutAndGetJSON(self, conn_tag, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)
    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)

    def test_AuthorizedAsPropNewOwner(self):
        doPutAndGetJSON(self, conn_prop, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)
        doPutAndGetJSON(self, conn_prop, self.T1, T1p_empty, 204, self.T1, T1p_empty_r, 200)
    def test_AuthorizedAsAdminNewOwner(self):
        doPutAndGetJSON(self, conn_admin, self.T1, T1_empty, 204, self.T1, T1_empty_r, 200)
        doPutAndGetJSON(self, conn_admin, self.T1, T1p_empty, 204, self.T1, T1p_empty_r, 200)

# As '" + user_chan2 + "' user that does not belong to group of tag
    def test_AuthorizedAsChanny2(self):
        doPutAndFailMessageJSON(self, conn_chan2, self.T1, T1_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name> PUT and GET   extended version, with channels in payload
#############################################################################################
class PutOneTagWithChannels(unittest.TestCase):
    """Test PUT and GET on the .../tags/<name> target - with channels in the payload"""
    def setUp(self):
        self.T1 = 'resources/tags/T1'
        self.T2 = 'resources/tags/T2'
        self.t1 = 'resources/tags/t1'
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_empty)
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.T1, T1_C1, 401)

    def test_AuthorizedAsTag(self):
        doPutAndGetJSON(self, conn_tag, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)
    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)

    def test_AuthorizedAsTagNewChannel(self):
        doPutAndGetJSON(self, conn_tag, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doPutAndGetJSON(self, conn_tag, self.T1, T1_C2, 204, self.T1, T1_C2_r, 200)
    def test_AuthorizedAsAdminNewChannel(self):
        doPutAndGetJSON(self, conn_admin, self.T1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doPutAndGetJSON(self, conn_admin, self.T1, T1_C2, 204, self.T1, T1_C2_r, 200)

# Adding tag to non-existing channel
    def test_AuthorizedAsTagNonexChannel(self):
        doPutAndFailMessageJSON(self, conn_tag, self.T1, T1_C3, 404, "Channels specified in tag update do not exist")

# Payload and URL names do not match
    def test_AuthorizedAsTagLcPayload(self):
        doPutAndFailMessageJSON(self, conn_tag, self.T1, t1_C1, 400, "Specified tag name 'T1' and payload tag name 't1' do not match")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name> POST   simple version, no channels
#############################################################################################
class PostOneTag(unittest.TestCase):
    """Test POST and GET on the .../tags/<name> target - no channels in the payload"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.T1 = 'resources/tags/T1'
        self.t1 = 'resources/tags/t1'
        self.T2 = 'resources/tags/T2'
        self.T3 = 'resources/tags/T3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_C12)
        response = conn_admin.request_put(self.T2, headers=copy(jsonheader), body=T2_C2)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.T1, T1p_empty, 401)

# Set a new tag owner
# channy does not belong to testp, so no test as Tag at this point
    def test_AuthorizedAsPropNewOwner(self):
        doPostAndGetJSON(self, conn_prop, self.T1, T1p_empty, 204, self.c, Cs12_1T1p_2T12p_r, 200)
    def test_AuthorizedAsChanNewOwner(self):
        doPostAndGetJSON(self, conn_chan, self.T1, T1p_empty, 204, self.c, Cs12_1T1p_2T12p_r, 200)
    def test_AuthorizedAsAdminNewOwner(self):
        doPostAndGetJSON(self, conn_admin, self.T1, T1p_empty, 204, self.c, Cs12_1T1p_2T12p_r, 200)

# New tag owner (non member of new owner)
    def test_AuthorizedAsChanNewOwnerNonMemberNew(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T1, T1p_empty, 403, "User '" + user_tag + "' does not belong to owner group '"+testp+"' of tag 'T1'")

# New tag owner (non member of old owner)
    def test_AuthorizedAsChanNewOwnerNonMemberOld(self):
        doPostAndGetJSON(self, conn_chan, self.T1, T1p_empty, 204, self.c, Cs12_1T1p_2T12p_r, 200)
        doPostAndFailMessageJSON(self, conn_tag, self.T1, T1_empty, 403, "User '" + user_tag + "' does not belong to owner group '"+testp+"' of tag 'T1'")

# Set a new tag name
    def test_AuthorizedAsTagNewName(self):
        doPostAndGetJSON(self, conn_tag, self.T1, t1_empty, 204, self.c, Cs12_1t1_2t1T2_r, 200)
    def test_AuthorizedAsAdminNewName(self):
        doPostAndGetJSON(self, conn_admin, self.T1, t1_empty, 204, self.c, Cs12_1t1_2t1T2_r, 200)

# Non-existing tag
    def test_AuthorizedAsTagNonexTag(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T3, T3_C1, 404, "A tag named 'T3' does not exist")

# As '" + user_chan2 + "' user that does not belong to group of tag
    def test_AuthorizedAsChanny2(self):
        doPostAndFailMessageJSON(self, conn_chan2, self.T1, T1_empty, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.t1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name> POST   extended version, with channels in payload
#############################################################################################
class UpdateTagWithChannels(unittest.TestCase):
    """Test POST and GET on the .../tags/<name> target - with channels in the payload"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.C3 = 'resources/channels/C3'
        self.c = 'resources/channels'
        self.T1 = 'resources/tags/T1'
        self.T2 = 'resources/tags/T2'
        self.T3 = 'resources/tags/T3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.C3, headers=copy(jsonheader), body=C3_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_C12)
        response = conn_admin.request_put(self.T2, headers=copy(jsonheader), body=T2_C2)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.T2, T2_C23, 401)

# Add a channel, test through .../channels GET and .../tags/T2 GET
    def test_AuthorizedAsTag(self):
        doPostAndGetJSON(self, conn_tag, self.T2, T2_C23, 204, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsProp(self):
        doPostAndGetJSON(self, conn_prop, self.T2, T2_C23, 204, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.T2, T2_C23, 204, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.T2, T2_C23, 204, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)

# Non-existing tag
    def test_AuthorizedAsTagNonexTag(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T3, T3_C1, 404, "A tag named 'T3' does not exist")

# Non-existing channel
    def test_AuthorizedAsTagNonexChannel(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T2, T2_C4, 404, "Channels specified in tag update do not exist")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C3, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name> DELETE
#############################################################################################
class DeleteTag(unittest.TestCase):
    """Test DELETE on the .../tags/<name> target"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.C3 = 'resources/channels/C3'
        self.c = 'resources/channels'
        self.T1 = 'resources/tags/T1'
        self.T2 = 'resources/tags/T2'
        self.TX = 'resources/tags/TX'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_C12)
        response = conn_admin.request_put(self.T2, headers=copy(jsonheader), body=T2_C2)

    def test_Unauthorized(self):
        doDeleteAndFailJSON(self, conn_none, self.T1, 401)

    def test_AuthorizedAsTag(self):
        doDeleteAndGetJSON(self, conn_tag, self.T1, 200, self.T1, "", 404)
        doGetJSON(self, conn_tag, self.c, Cs12_1e2t2_r, 200)
    def test_AuthorizedAsProp(self):
        doDeleteAndGetJSON(self, conn_prop, self.T1, 200, self.T1, "", 404)
        doGetJSON(self, conn_prop, self.c, Cs12_1e2t2_r, 200)
    def test_AuthorizedAsChan(self):
        doDeleteAndGetJSON(self, conn_chan, self.T1, 200, self.T1, "", 404)
        doGetJSON(self, conn_chan, self.c, Cs12_1e2t2_r, 200)
    def test_AuthorizedAsAdmin(self):
        doDeleteAndGetJSON(self, conn_admin, self.T1, 200, self.T1, "", 404)
        doGetJSON(self, conn_admin, self.c, Cs12_1e2t2_r, 200)

# same as channy2 (not member of testt)
    def test_AuthorizedAsChanny2GroupNonMember(self):
        doDeleteAndFailMessageJSON(self, conn_chan2, self.T1, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

# delete nonexisting tag
    def test_AuthorizedAsTagNonexistingTag(self):
        doDeleteAndFailJSON(self, conn_tag, self.TX, 404)
    def test_AuthorizedAsAdminNonexistingTag(self):
        doDeleteAndFailJSON(self, conn_admin, self.TX, 404)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name>/<channel> PUT
#############################################################################################
class AddSingleTag(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.T1 = 'resources/tags/T1'
        self.T1C1 = 'resources/tags/T1/C1'
        self.T1C3 = 'resources/tags/T1/C3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_empty)

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.T1C1, T1_C1, 401)

    def test_AuthorizedAsTag(self):
        doPutAndGetJSON(self, conn_tag, self.T1C1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doGetJSON(self, conn_tag, self.c, Cs12_1T1_r, 200)
    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.T1C1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doGetJSON(self, conn_prop, self.c, Cs12_1T1_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.T1C1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doGetJSON(self, conn_chan, self.c, Cs12_1T1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.T1C1, T1_C1, 204, self.T1, T1_C1_r, 200)
        doGetJSON(self, conn_admin, self.c, Cs12_1T1_r, 200)

# same as channy2 (not member of testt)
    def test_AuthorizedAsChanny2GroupNonMember(self):
        doPutAndFailMessageJSON(self, conn_chan2, self.T1C1, T1_C1, 403, "User '"+  user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

# Adding tag to non-existing channel
    def test_AuthorizedAsTagNonexChannel(self):
        doPutAndFailMessageJSON(self, conn_tag, self.T1C3, T1_C3, 404, "Channels specified in tag update do not exist")

# Payload and URL names do not match
    def test_AuthorizedAsTagLcPayload(self):
        doPutAndFailMessageJSON(self, conn_tag, self.T1C1, t1_C1, 400, "Specified tag name 'T1' and payload tag name 't1' do not match")

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))


#############################################################################################
# Test .../tags/<name>/<channel> DELETE
#############################################################################################
class DeleteSingleTag(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.T1 = 'resources/tags/T1'
        self.T1C1 = 'resources/tags/T1/C1'
        self.TXC1 = 'resources/tags/TX/C1'
        self.T1CX = 'resources/tags/T1/CX'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_C12)

    def test_Unauthorized(self):
        doDeleteAndFailJSON(self, conn_none, self.T1C1, 401)

    def test_AuthorizedAsTag(self):
        doDeleteAndGetJSON(self, conn_tag, self.T1C1, 200, self.T1, T1_C2_r, 200)
        doGetJSON(self, conn_tag, self.c, Cs12_1e2T1_r, 200)
    def test_AuthorizedAsProp(self):
        doDeleteAndGetJSON(self, conn_prop, self.T1C1, 200, self.T1, T1_C2_r, 200)
        doGetJSON(self, conn_prop, self.c, Cs12_1e2T1_r, 200)
    def test_AuthorizedAsChan(self):
        doDeleteAndGetJSON(self, conn_chan, self.T1C1, 200, self.T1, T1_C2_r, 200)
        doGetJSON(self, conn_chan, self.c, Cs12_1e2T1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doDeleteAndGetJSON(self, conn_admin, self.T1C1, 200, self.T1, T1_C2_r, 200)
        doGetJSON(self, conn_admin, self.c, Cs12_1e2T1_r, 200)

# same as channy2 (not member of testt)
    def test_AuthorizedAsChanny2GroupNonMember(self):
        doDeleteAndFailMessageJSON(self, conn_chan2, self.T1C1, 403, "User '" + user_chan2 + "' does not belong to owner group '" + testt + "' of tag 'T1'")

# delete nonexisting tag
    def test_AuthorizedAsTagNonexTag(self):
        doDeleteAndFailJSON(self, conn_tag, self.TXC1, 404)

# delete nonexisting channel
    def test_AuthorizedAsTagNonexChannel(self):
        doDeleteAndFailJSON(self, conn_tag, self.T1CX, 404)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))


#############################################################################################
# Test .../properties PUT                          not implemented
#############################################################################################
class PutManyProperties(unittest.TestCase):
    def setUp(self):
        self.P1 = 'resources/properties/P1'
        self.p = 'resources/properties'

    def test_AuthorizedAsAdmin(self):
        doPutAndFailJSON(self, conn_admin, self.p, Ps1t_empty, 405)

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))


#############################################################################################
# Test .../properties POST and GET
#############################################################################################
class PostManyProperties(unittest.TestCase):
    """Test POST and GET on the .../properties target"""
    def setUp(self):
        self.urlp = 'resources/properties'
        self.P1 = 'resources/properties/P1'
        self.P2 = 'resources/properties/P2'

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.urlp, Ps12_empty, 401)

    def test_AuthorizedAsTag(self):
        doPostAndFailJSON(self, conn_tag, self.urlp, Ps12_empty, 403)

    def test_AuthorizedAsProp(self):
        doPostAndGetJSON(self, conn_prop, self.urlp, Ps12_empty, 204, self.urlp, Ps12_empty_r, 200)
    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.urlp, Ps12_empty, 204, self.urlp, Ps12_empty_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.urlp, Ps12_empty, 204, self.urlp, Ps12_empty_r, 200)

    def doTestAndCheckOverwrite(self, conn):
        response = conn.request_post(self.urlp, headers=copy(jsonheader), body=Ps12_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn.request_post(self.urlp, headers=copy(jsonheader), body=Ps12t_empty)
        self.failUnlessEqual('204', response[u'headers']['status'])
        response = conn_none.request_get(self.urlp, headers=copy(jsonheader))
        self.failUnlessEqual('200', response[u'headers']['status'])
        j1 = JSONDecoder().decode(response[u'body'])
        self.failUnlessEqual(j1, Ps12t_empty_r)
    def test_AuthorizedAsPropOverwrite(self):
        doPostAndGetJSON(self, conn_prop, self.urlp, Ps12_empty, 204, self.urlp, Ps12_empty_r, 200)
        doPostAndGetJSON(self, conn_prop, self.urlp, Ps12t_empty, 204, self.urlp, Ps12t_empty_r, 200)
    def test_AuthorizedAsAdminOverwrite(self):
        doPostAndGetJSON(self, conn_admin, self.urlp, Ps12_empty, 204, self.urlp, Ps12_empty_r, 200)
        doPostAndGetJSON(self, conn_admin, self.urlp, Ps12t_empty, 204, self.urlp, Ps12t_empty_r, 200)

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2Overwrite(self):
        doPostAndFailMessageJSON(self, conn_prop2, self.urlp, Ps12_empty, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P2, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name> PUT and GET   simple version, no channels
#############################################################################################
class PutOneProperty(unittest.TestCase):
    def setUp(self):
        self.P1 = 'resources/properties/P1'

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.P1, P1_empty, 401)

    def test_AuthorizedAsTag(self):
        doPutAndFailJSON(self, conn_tag, self.P1, P1_empty, 403)

    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.P1, P1_empty, 204, self.P1, P1_empty_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.P1, P1_empty, 204, self.P1, P1_empty_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.P1, P1_empty, 204, self.P1, P1_empty_r, 200)

    def test_AuthorizedAsPropNewOwner(self):
        doPutAndGetJSON(self, conn_prop, self.P1, P1_empty, 204, self.P1, P1_empty_r, 200)
        doPutAndGetJSON(self, conn_prop, self.P1, P1t_empty, 204, self.P1, P1t_empty_r, 200)
    def test_AuthorizedAsAdminNewOwner(self):
        doPutAndGetJSON(self, conn_admin, self.P1, P1_empty, 204, self.P1, P1_empty_r, 200)
        doPutAndGetJSON(self, conn_admin, self.P1, P1t_empty, 204, self.P1, P1t_empty_r, 200)

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2(self):
        doPutAndFailMessageJSON(self, conn_prop2, self.P1, P1_empty, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name> PUT and GET   extended version, with channels in payload
#############################################################################################
class PutOnePropertyWithChannels(unittest.TestCase):
    def setUp(self):
        self.P1 = 'resources/properties/P1'
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_empty)
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.P1, P1_C1, 401)

    def test_AuthorizedAsTag(self):
        doPutAndFailJSON(self, conn_tag, self.P1, P1_C1, 403)

    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.P1, P1_C1, 204, self.P1, P1_C1_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.P1, P1_C1, 204, self.P1, P1_C1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.P1, P1_C1, 204, self.P1, P1_C1_r, 200)

    def test_AuthorizedAsPropNewChannel(self):
        doPutAndGetJSON(self, conn_prop, self.P1, P1_C1, 204, self.P1, P1_C1_r, 200)
        doPutAndGetJSON(self, conn_prop, self.P1, P1_C2, 204, self.P1, P1_C2_r, 200)
    def test_AuthorizedAsAdminNewChannel(self):
        doPutAndGetJSON(self, conn_admin, self.P1, P1_C1, 204, self.P1, P1_C1_r, 200)
        doPutAndGetJSON(self, conn_admin, self.P1, P1_C2, 204, self.P1, P1_C2_r, 200)

# Adding property to non existing channel
    def test_AuthorizedAsPropNonexChannel(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1, P1_C3, 404, "Channels specified in property update do not exist")

# Payload and URL names do not match
    def test_AuthorizedAsPropLcPayload(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1, p1_C1, 400, "Specified property name 'P1' and payload property name 'p1' do not match")

# Invalid payload (missing or null or empty property value in embedded list)
    def test_AuthorizedAsPropNullPropValue(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPutAndFailMessageJSON(self, conn_admin, self.P1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsPropEmptyPropValue(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminEmptyPropValue(self):
        doPutAndFailMessageJSON(self, conn_admin, self.P1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name> POST   simple version, no channels
#############################################################################################
class PostOneProperty(unittest.TestCase):
    """Test POST and GET on the .../properties/<name> target - no channels in the payload"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.p1 = 'resources/properties/p1'
        self.P2 = 'resources/properties/P2'
        self.P3 = 'resources/properties/P3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_C12)
        response = conn_admin.request_put(self.P2, headers=copy(jsonheader), body=P2_C2)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.P1, P1t_empty, 401)
    def test_AuthorizedAsTag(self):
        doPostAndFailJSON(self, conn_tag, self.P1, P1t_empty, 403)

# Set a new property owner
    def test_AuthorizedAsPropNewOwner(self):
        doPostAndGetJSON(self, conn_prop, self.P1, P1t_empty, 204, self.c, Cs12_1P1t_2P12t_r, 200)
    def test_AuthorizedAsChanNewOwner(self):
        doPostAndGetJSON(self, conn_chan, self.P1, P1t_empty, 204, self.c, Cs12_1P1t_2P12t_r, 200)
    def test_AuthorizedAsAdminNewOwner(self):
        doPostAndGetJSON(self, conn_admin, self.P1, P1t_empty, 204, self.c, Cs12_1P1t_2P12t_r, 200)

# Set a new property name
    def test_AuthorizedAsPropNewName(self):
        doPostAndGetJSON(self, conn_prop, self.P1, p1_empty, 204, self.c, Cs12_1p1_2p1P2_r, 200)
    def test_AuthorizedAsAdminNewName(self):
        doPostAndGetJSON(self, conn_admin, self.P1, p1_empty, 204, self.c, Cs12_1p1_2p1P2_r, 200)

# New tag owner (non member of new owner)
    def test_AuthorizedAsChanNewOwnerNonMemberNew(self):
        doPostAndFailMessageJSON(self, conn_prop2, self.P1, P1t_empty, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

# New tag owner (non member of old owner)
    def test_AuthorizedAsChanNewOwnerNonMemberOld(self):
        doPostAndGetJSON(self, conn_prop, self.P1, P1t_empty, 204, self.c, Cs12_1P1t_2P12t_r, 200)
        doPostAndFailMessageJSON(self, conn_prop2, self.P1, P1_empty, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

# Non-existing property
    def test_AuthorizedAsPropNonexProp(self):
        doPostAndFailMessageJSON(self, conn_prop, self.P3, P3_empty, 404, "A property named 'P3' does not exist")

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2(self):
        doPostAndFailMessageJSON(self, conn_prop2, self.P1, P1_empty, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.p1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name> POST   extended version, with channels in payload
#############################################################################################
class UpdatePropertyWithChannels(unittest.TestCase):
    """Test POST and GET on the .../properties/<name> target - with channels in the payload"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.C3 = 'resources/channels/C3'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.P2 = 'resources/properties/P2'
        self.P3 = 'resources/properties/P3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.C3, headers=copy(jsonheader), body=C3_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_C12)
        response = conn_admin.request_put(self.P2, headers=copy(jsonheader), body=P2_C2)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.P2, P2_C23, 401)

    def test_AuthorizedAsTag(self):
        doPostAndFailJSON(self, conn_tag, self.P2, P2_C23, 403)

# Add a channel, test through .../channels GET and .../properties/P2 GET
    def test_AuthorizedAsProp(self):
        doPostAndGetJSON(self, conn_prop, self.P2, P2_C23, 204, self.c, Cs123_1P1_2P12_3P2_r, 200)
        doGetJSON(self, conn_prop, self.P2, P2_C23_r, 200)
    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.P2, P2_C23, 204, self.c, Cs123_1P1_2P12_3P2_r, 200)
        doGetJSON(self, conn_tag, self.P2, P2_C23_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.P2, P2_C23, 204, self.c, Cs123_1P1_2P12_3P2_r, 200)
        doGetJSON(self, conn_tag, self.P2, P2_C23_r, 200)

# Change value, test through .../channels GET and .../properties/P2 GET
    def test_AuthorizedAsPropNewValue(self):
        doPostAndGetJSON(self, conn_prop, self.P2, P2_C1v, 204, self.c, Cs123_1P12v_2P12_3e_r, 200)
        doGetJSON(self, conn_prop, self.P2, P2_C1v2_r, 200)
    def test_AuthorizedAsAdminNewValue(self):
        doPostAndGetJSON(self, conn_admin, self.P2, P2_C1v, 204, self.c, Cs123_1P12v_2P12_3e_r, 200)
        doGetJSON(self, conn_tag, self.P2, P2_C1v2_r, 200)

# Invalid payload (missing or null or empty property value in embedded list)
    def test_AuthorizedAsPropNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_prop, self.P1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPostAndFailMessageJSON(self, conn_admin, self.P1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsPropEmptyPropValue(self):
        doPostAndFailMessageJSON(self, conn_prop, self.P1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminEmptyPropValue(self):
        doPostAndFailMessageJSON(self, conn_admin, self.P1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")

# Non-existing tag
    def test_AuthorizedAsPropNonexTag(self):
        doPostAndFailMessageJSON(self, conn_prop, self.P3, P3_C1, 404, "A property named 'P3' does not exist")

# Non-existing channel
    def test_AuthorizedAsPropNonexChannel(self):
        doPostAndFailMessageJSON(self, conn_prop, self.P2, P2_C4, 404, "Channels specified in property update do not exist")

    def tearDown(self):
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C3, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name> DELETE
#############################################################################################
class DeleteProperty(unittest.TestCase):
    """Test DELETE on the .../properties/<name> target"""
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.P2 = 'resources/properties/P2'
        self.PX = 'resources/properties/PX'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_C12)
        response = conn_admin.request_put(self.P2, headers=copy(jsonheader), body=P2_C2)

    def test_Unauthorized(self):
        doDeleteAndFailJSON(self, conn_none, self.P1, 401)
    def test_AuthorizedAsTag(self):
        doDeleteAndFailJSON(self, conn_tag, self.P1, 403)

    def test_AuthorizedAsProp(self):
        doDeleteAndGetJSON(self, conn_prop, self.P1, 200, self.P1, "", 404)
        doGetJSON(self, conn_prop, self.c, Cs12_1e2p2_r, 200)
    def test_AuthorizedAsChan(self):
        doDeleteAndGetJSON(self, conn_chan, self.P1, 200, self.P1, "", 404)
        doGetJSON(self, conn_chan, self.c, Cs12_1e2p2_r, 200)
    def test_AuthorizedAsAdmin(self):
        doDeleteAndGetJSON(self, conn_admin, self.P1, 200, self.P1, "", 404)
        doGetJSON(self, conn_admin, self.c, Cs12_1e2p2_r, 200)

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2NonMember(self):
        doDeleteAndFailMessageJSON(self, conn_prop2, self.P1, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

    def test_AuthorizedAsPropNonexistingProperty(self):
        doDeleteAndFailJSON(self, conn_prop, self.PX, 404)
    def test_AuthorizedAsAdminNonexistingProperty(self):
        doDeleteAndFailJSON(self, conn_admin, self.PX, 404)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name>/<channel> PUT
#############################################################################################
class AddSingleProperty(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.P1C1 = 'resources/properties/P1/C1'
        self.P1C3 = 'resources/properties/P1/C3'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_empty)

    def test_Unauthorized(self):
        doPutAndFailJSON(self, conn_none, self.P1C1, P1_C1, 401)
    def test_AuthorizedAsTag(self):
        doPutAndFailJSON(self, conn_tag, self.P1C1, P1_C1, 403)

    def test_AuthorizedAsProp(self):
        doPutAndGetJSON(self, conn_prop, self.P1C1, P1_C1, 204, self.P1, P1_C1_r, 200)
        doGetJSON(self, conn_prop, self.c, Cs12_1P1_r, 200)
    def test_AuthorizedAsChan(self):
        doPutAndGetJSON(self, conn_chan, self.P1C1, P1_C1, 204, self.P1, P1_C1_r, 200)
        doGetJSON(self, conn_chan, self.c, Cs12_1P1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPutAndGetJSON(self, conn_admin, self.P1C1, P1_C1, 204, self.P1, P1_C1_r, 200)
        doGetJSON(self, conn_admin, self.c, Cs12_1P1_r, 200)

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2NonMember(self):
        doPutAndFailMessageJSON(self, conn_prop2, self.P1C1, P1_C1, 403, "User '" + user_prop2 + "' does not belong to owner group '" + testp + "' of property 'P1'")

# Invalid payload (missing or null or empty property value in embedded list)
    def test_AuthorizedAsPropNullPropValue(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1C1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminNullPropValue(self):
        doPutAndFailMessageJSON(self, conn_admin, self.P1C1, P1_C1_P1nv, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsPropEmptyPropValue(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1C1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")
    def test_AuthorizedAsAdminEmptyPropValue(self):
        doPutAndFailMessageJSON(self, conn_admin, self.P1C1, P1_C1_P1ev, 400, "Invalid property value (missing or null or empty string) for 'P1'")

# Adding property to non-existing channel
    def test_AuthorizedAsPropNonexChannel(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1C3, P1_C3, 404, "Channels specified in property update do not exist")

# Payload and URL names do not match
    def test_AuthorizedAsPropLcPayload(self):
        doPutAndFailMessageJSON(self, conn_prop, self.P1C1, p1_C1, 400, "Specified property name 'P1' and payload property name 'p1' do not match")

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))


#############################################################################################
# Test .../properties/<name>/<channel> DELETE
#############################################################################################
class DeleteSingleProperty(unittest.TestCase):
    def setUp(self):
        self.C1 = 'resources/channels/C1'
        self.C2 = 'resources/channels/C2'
        self.c = 'resources/channels'
        self.P1 = 'resources/properties/P1'
        self.P1C1 = 'resources/properties/P1/C1'
        self.PXC1 = 'resources/properties/PX/C1'
        self.P1CX = 'resources/properties/P1/CX'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.P1, headers=copy(jsonheader), body=P1_C12)

    def test_Unauthorized(self):
        doDeleteAndFailJSON(self, conn_none, self.P1C1, 401)
    def test_AuthorizedAsTag(self):
        doDeleteAndFailJSON(self, conn_tag, self.P1C1, 403)

    def test_AuthorizedAsProp(self):
        doDeleteAndGetJSON(self, conn_prop, self.P1C1, 200, self.P1, P1_C2_r, 200)
        doGetJSON(self, conn_prop, self.c, Cs12_1e2P1_r, 200)
    def test_AuthorizedAsChan(self):
        doDeleteAndGetJSON(self, conn_chan, self.P1C1, 200, self.P1, P1_C2_r, 200)
        doGetJSON(self, conn_chan, self.c, Cs12_1e2P1_r, 200)
    def test_AuthorizedAsAdmin(self):
        doDeleteAndGetJSON(self, conn_admin, self.P1C1, 200, self.P1, P1_C2_r, 200)
        doGetJSON(self, conn_admin, self.c, Cs12_1e2P1_r, 200)

# As '" + user_prop2 + "' user that does not belong to group of property
    def test_AuthorizedAsProppy2NonMember(self):
        doDeleteAndFailMessageJSON(self, conn_prop2, self.P1C1, 403, "User '" + user_prop2 + "' does not belong to owner group '"+testp+"' of property 'P1'")

# delete nonexisting tag
    def test_AuthorizedAsPropNonexTag(self):
        doDeleteAndFailJSON(self, conn_prop, self.PXC1, 404)

# delete nonexisting channel
    def test_AuthorizedAsPropNonexChannel(self):
        doDeleteAndFailJSON(self, conn_prop, self.P1CX, 404)

    def tearDown(self):
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.P1, headers=copy(jsonheader))


if __name__ == '__main__':

# Check if database is empty
    getextra = ""
    response = conn_none.request_get('resources/channels', headers=copy(jsonheader))
    assert '200' == response[u'headers']['status'], 'Database list request returned an error'
    j1 = JSONDecoder().decode(response[u'body'])
    if (None != j1[u'channels']):
        print "Database at " + base_url + " not empty."
        d = raw_input('Continue anyway? [y/N] ')
        if d != "y" and d != "Y":
            sys.exit(1)
        dbnonempty = True
        getextra = "?~name=C?"
    unittest.main()
