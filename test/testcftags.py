import unittest
from time import sleep
try: from json import JSONDecoder, JSONEncoder
except ImportError: from simplejson import JSONDecoder, JSONEncoder
from restful_lib import Connection
from copy import copy
import sys, os

from _testConf import _testConf

base_url = os.getenv("BASEURL")
if base_url is None:
    base_url = _testConf.get('DEFAULT', 'BaseURL')

secure_url = os.getenv("SECUREURL")
if secure_url is None:
    secure_url = _testConf.get('DEFAULT', 'SecureURL')

user_tag = _testConf.get('DEFAULT', 'tagUsername')
user_prop = _testConf.get('DEFAULT', 'propUsername')
user_prop2 =_testConf.get('DEFAULT', 'propUsername2')
user_chan = _testConf.get('DEFAULT', 'channelUsername')
user_chan2 =_testConf.get('DEFAULT', 'channelUsername2')
user_admin = _testConf.get('DEFAULT', 'username')

conn_none  = Connection(base_url)
conn_none_secure = Connection(secure_url)
conn_tag   = Connection(secure_url, username=user_tag,   \
                        password=_testConf.get('DEFAULT', 'tagPassword'))
conn_tag_plain = Connection(base_url, username=user_tag,   \
                        password=_testConf.get('DEFAULT', 'tagPassword'))
conn_prop  = Connection(secure_url, username=user_prop,  \
                        password=_testConf.get('DEFAULT', 'propPassword'))
conn_prop_plain = Connection(base_url, username=user_prop,  \
                        password=_testConf.get('DEFAULT', 'propPassword'))
conn_prop2 = Connection(secure_url, username=user_prop2, \
                        password=_testConf.get('DEFAULT', 'propPassword2'))
conn_chan  = Connection(secure_url, username=user_chan,  \
                        password=_testConf.get('DEFAULT', 'channelPassword'))
conn_chan_plain = Connection(base_url, username=user_chan,  \
                        password=_testConf.get('DEFAULT', 'channelPassword'))
conn_chan2 = Connection(secure_url, username=user_chan2, \
                        password=_testConf.get('DEFAULT', 'channelPassword2'))
conn_admin = Connection(secure_url, username=user_admin, \
                        password=_testConf.get('DEFAULT', 'password'))
conn_admin_plain = Connection(base_url, username=user_admin, \
                        password=_testConf.get('DEFAULT', 'password'))

jsonheader = {'content-type':'application/json','accept':'application/json'}
xmlheader = {'content-type':'application/xml','accept':'application/xml'}

testc=_testConf.get('DEFAULT', 'channelOwner')
testp=_testConf.get('DEFAULT', 'propOwner')
testt=_testConf.get('DEFAULT', 'tagOwner')


#     Channels
C1_empty = '{"name": "C1", "owner": "' + testc + '"}'
C1_empty_r = {u'owner': testc, u'name': u'C1', u'properties': [], u'tags': []}
c1_empty = '{"name": "c1", "owner": "' + testc + '"}'
C1t_empty = '{"name": "C1", "owner": "' + testt + '"}'
C1nn_empty = '{"owner": "' + testc + '"}'
C1no_empty = '{"name": "C1"}'
C1en_empty = '{"name": "", "owner": "' + testc + '"}'
C1eo_empty = '{"name": "C1", "owner": ""}'
C2_empty = '{"name": "C2", "owner": "' + testc + '"}'
C3_empty = '{"name": "C3", "owner": "' + testc + '"}'

C1_full = '{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}, {"name": "P2", "value": "prop2"}], "tags": [{"name": "T1"}, {"name": "T2"}]}'
C2_full = '{"name": "C2", "owner": "' + testc + '", "properties": [{"name": "P3", "value": "prop3"}, {"name": "P4", "value": "prop4"}], "tags": [{"name": "T33"}, {"name": "T44"}]}'
C3_full = '{"name": "C3", "owner": "' + testc + '", "properties": [{"name": "P3", "value": "prop1"}, {"name": "P2", "value": "prop2"}], "tags": [{"name": "T33"}, {"name": "T2"}]}'
C4_full = '{"name": "C4", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}, {"name": "P4", "value": "prop4"}], "tags": [{"name": "T1"}, {"name": "T44"}]}'

C1_full2 = '{"name": "C1", "owner": "' + testc + '", "properties":[{"name": "P1", "value": "prop11"}, {"name": "P2", "value": "prop22"}], "tags":[{"name": "T1"}, {"name": "T44"}]}'
C1_full2_r = {u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop11'}, {u'owner': testp, u'name': u'P2', u'value': u'prop22'}], u'tags': [{u'owner': testt, u'name': u'T1'}, {u'owner': testt, u'name': u'T2'}, {u'owner': testt, u'name': u'T44'}]}

c1_full_r = {u'owner': testc, u'name': u'c1', u'properties':[{u'owner': None, u'name': u'P1', u'value': u'prop1'}, {u'owner': None, u'name': u'P2', u'value': u'prop2'}], u'tags': [{u'owner': None, u'name': u'T1'}, {u'owner': None, u'name': u'T2'}]}
C1t_full_r = {u'owner': testt, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': [{u'owner': testt, u'name': u'T1'}, {u'owner': testt, u'name': u'T2'}]}

Cs1234_full = '[' + C1_full + ', ' + C2_full + ', '+ C3_full + ', '+ C4_full + ']'
Cs1_full = '[' + C1_full + ']'
Cs121_full = '[' + C1_full + ', ' + C2_full + ', '+ C1_full + ']'
Cs12_1nn_empty = '[' + C1nn_empty + ', ' + C2_empty + ']'
Cs12_1no_empty = '[' + C1no_empty + ', ' + C2_empty + ']'
Cs12_1en_empty = '[' + C1en_empty + ', ' + C2_empty + ']'
Cs12_1eo_empty = '[' + C1eo_empty + ', ' + C2_empty + ']'
C1_full_r = {u'owner': testc, u'name': u'C1', u'properties':[{u'owner': None, u'name': u'P1', u'value': u'prop1'}, {u'owner': None, u'name': u'P2', u'value': u'prop2'}], u'tags': [{u'owner': None, u'name': u'T1'}, {u'owner': None, u'name': u'T2'}]}
C2_full_r = {u'owner': testc, u'name': u'C2', u'properties':[{u'owner': None, u'name': u'P3', u'value': u'prop3'}, {u'owner': None, u'name': u'P4', u'value': u'prop4'}], u'tags': [{u'owner': None, u'name': u'T33'}, {u'owner': None, u'name': u'T44'}]}
C3_full_r = {u'owner': testc, u'name': u'C3', u'properties':[{u'owner': None, u'name': u'P2', u'value': u'prop2'}, {u'owner': None, u'name': u'P3', u'value': u'prop1'}], u'tags': [{u'owner': None, u'name': u'T2'}, {u'owner': None, u'name': u'T33'}]}
C4_full_r = {u'owner': testc, u'name': u'C4', u'properties':[{u'owner': None, u'name': u'P1', u'value': u'prop1'}, {u'owner': None, u'name': u'P4', u'value': u'prop4'}], u'tags': [{u'owner': None, u'name': u'T1'}, {u'owner': None, u'name': u'T44'}]}
C1eP1_full_r = {u'owner': testc, u'name': u'C1', u'properties':[{u'owner': None, u'name': u'P2', u'value': u'prop2'}], u'tags': {u'tag': [{u'owner': None, u'name': u'T1'}, {u'owner': None, u'name': u'T2'}]}}
Cs1234_full_r = [C1_full_r, C2_full_r, C3_full_r, C4_full_r]
Cs12_1t_2_empty = '[' + C1t_empty + ', ' + C2_empty + ']'
Cs12_full = '[' + C1_full + ', ' + C2_full + ']'
Cs12_full_r = [C1_full_r, C2_full_r]
Cs13_full_r = [C1_full_r, C3_full_r]
Cs23_full_r = [C2_full_r, C3_full_r]
Cs1_full_r = [C1_full_r]
Cs3_full_r = [C3_full_r]
Cs4_full_r = [C4_full_r]
None_r = []

# tests with empty, null and no value for property
P1ev = '{"name": "P1", "value": ""}'
P1nv = '{"name": "P1"}'
P3ev = '{"name": "P3", "value": ""}'

C1_P1ev = '{"name": "C1", "owner": "' + testc + '", "properties": [' + P1ev + ']}'
C1_P1nv = '{"name": "C1", "owner": "' + testc + '", "properties": [' + P1nv + ']}'
C1_P3ev = '{"name": "C1", "owner": "' + testc + '", "properties": [' + P3ev + ']}'

Cs12_P1ev = '[' + C1_P1ev + ', ' + C2_full + ']'
Cs12_P1nv = '[' + C1_P1nv + ', ' + C2_full + ']'

# replies needed for tags and properties URLs
Cs12_1e2t2_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T2'}]}]
Cs12_1e2p2_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': []}, {u'owner': testc, u'name': u'C2', u'tags': [], u'properties':[{u'owner': testp, u'name': u'P2', u'value': u'prop2'}]}]
Cs12_1T1p_2T12p_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': [{u'owner': testp, u'name': u'T1'}]}, {u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testp, u'name': u'T1'}, {u'owner': testt, u'name': u'T2'}]}]
Cs12_1t1_2t1T2_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': [{u'owner': testt, u'name': u't1'}]}, {u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testt, u'name': u't1'}, {u'owner': testt, u'name': u'T2'}]}]
Cs123_1T1_2T12_3T2_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T1'}]}, {u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T1'}, {u'owner': testt, u'name': u'T2'}]}, {u'owner': testc, u'name': u'C3', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T2'}]}]
Cs12_1P1t_2P12t_r = [{u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testt, u'name': u'P1', u'value': u'prop1'}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testt, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': []}]
Cs12_1p1_2p1P2_r = [{u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'p1', u'value': u'prop1'}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'p1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': []}]
Cs123_1P1_2P12_3P2_r = [{u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop1'}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': []}, {u'owner': testc, u'name': u'C3', u'properties':[{u'owner': testp, u'name': u'P2', u'value': u'prop3'}], u'tags': []}]
Cs123_1P12v_2P12_3e_r = [{u'owner': testc, u'name': u'C1', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'propv'}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': []}, {u'owner': testc, u'name': u'C3', u'properties': [], u'tags': []}]
Cs12_1T1_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T1'}]}, {u'owner': testc, u'name': u'C2', u'tags': [], u'properties': []}]
Cs12_1e2T1_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testt, u'name': u'T1'}]}]
Cs12_1P1_r = [{u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop1'}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'tags': [], u'properties': []}]
Cs12_1e2P1_r = [{u'owner': testc, u'name': u'C1', u'properties': [], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop1'}], u'tags': []}]
Cs2_P12_r = [{u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1'}, {u'owner': testp, u'name': u'P2', u'value': u'prop2'}], u'tags': []}]

#     Properties
P1 = '{"name": "P1", "value": "prop1"}'
p1 = '{"name": "p1", "value": "prop1"}'
P1_empty = '{"name": "P1", "owner": "' + testp + '"}'
P1_empty_r = {u'owner': testp, u'name': u'P1', u'channels': [], u'value':None}
p1_empty = '{"name": "p1", "owner": "' + testp + '"}'
P1t_empty = '{"name": "P1", "owner": "' + testt + '"}'
P1t_empty_r = {u'owner': testt, u'name': u'P1', u'channels': [], u'value':None}
P2_empty = '{"name": "P2", "owner": "' + testp + '"}'
P2t_empty = '{"name": "P2", "owner": "' + testt + '"}'
P3_empty = '{"name": "P3", "owner": "' + testp + '"}'
P4_empty = '{"name": "P4", "owner": "' + testp + '"}'

Ps1t_empty = '[ ' + P1t_empty + ']'
Ps12t_empty = '[' + P1t_empty + ', ' + P2t_empty + ']'
Ps12_empty_xml = "<properties><property name=\"P1\" owner=\"testp\"/><property name=\"P2\" owner=\"testp\"/></properties>"
Ps12_empty = '[' + P1_empty + ', ' + P2_empty + ']'
Ps12_empty_r = [{u'owner': testp, u'name': u'P1', u'value':None}, {u'owner': testp, u'name': u'P2', u'value':None}]
Ps12t_empty_r = [{u'owner': testt, u'name': u'P1', u'value':None}, {u'owner': testt, u'name': u'P2', u'value':None}]
Ps1234_empty = '[' + P1_empty + ', ' + P2_empty + ', ' + P3_empty+ ', ' + P4_empty + ']'

P1_C1 = '{"name": "P1", "owner": "' + testp + '", "value": "prop1", "channels": [{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}]}]}}'
P1_C1_r = {u'channels': [{u'owner': testc, u'name': u'C1', u'properties':[{u'owner': testp, u'name': u'P1', u'value': u'prop1', u'channels': []}], u'tags': []}], u'owner': testp, u'name': u'P1', u'value':None}
P1_C1_P1nv = '{"name": "P1", "owner": "' + testp + '", "channels":[{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "P1"}]}]}'
P1_C1_P1ev = '{"name": "P1", "owner": "' + testp + '", "channels":[{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "P1", "value": ""}]}]}'
p1_C1 = '{"name": "p1", "owner": "' + testp + '", "channels":[{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "p1", "value": "prop1"}]}]}'
P1_C2 = '{"name": "P1", "owner": "' + testp + '", "value": "prop1", "channels":[{"name": "C2", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}, {"name": "P2", "value": "prop2"}]}]}'
P1_C2_r = {u'channels': [{u'owner': testc, u'name': u'C2', u'properties':[{u'owner': testp, u'channels': [], u'name': u'P1', u'value': u'prop1'}], u'tags': []}], u'owner': testp, u'name': u'P1', u'value':None}
P1_C12 = '{"name": "P1", "owner": "' + testp + '", "channels":[{"name": "C1", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}]}, {"name": "C2", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}]}]}'
P1_C3 = '{"name": "P1", "owner": "' + testp + '", "channels":[{"name": "C3", "owner": "' + testc + '", "properties": [{"name": "P1", "value": "prop1"}]}]}'
P2_C2 = '{"name": "P2", "owner": "' + testp + '", "channels":[{"name": "C2", "owner": "' + testc + '", "properties": [{"name": "P2", "value": "prop2"}]}]}'
P2_C1 = '{"name": "P2", "channels": [{"name": "C1", "properties": [{"name": "P2", "value": "prop2"}]}]}'
P2_C23 = '{"name": "P2", "owner": "' + testp + '", "channels": [{"name": "C2", "properties": [{"name": "P2", "value": "prop2"}]}, {"name": "C3", "properties": [{"name": "P2", "value": "prop3"}]}]}'
P2_C23_r = {u'channels': [{u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1', u'channels': []}, {u'owner': testp, u'name': u'P2', u'value': u'prop2', u'channels': []}], u'tags': []}, {u'owner': testc, u'name': u'C3', u'properties':[{u'owner': testp, u'name': u'P2', u'value': u'prop3', u'channels': []}], u'tags': []}], u'owner': testp, u'name': u'P2', u'value': None}
P2_C4 = '{"name": "P2", "channels": [{"name": "C4", "properties": [{"name": "P2", "value": "prop2"}]}]}'
P3_C1 = '{"name": "P3", "channels": [{"name": "C1", "properties": [{"name": "P3", "value": "prop3"}]}]}'
P2_C1v = '{"name": "P2", "channels": [{"name": "C1", "properties": [{"name": "P2", "value": "propv"}]}]}'
P2_C1v2_r = {u'channels': [{u'owner': testc, u'name': u'C1', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1', u'channels': []}, {u'owner': testp, u'name': u'P2', u'value': u'propv', u'channels': []}], u'tags': []}, {u'owner': testc, u'name': u'C2', u'properties': [{u'owner': testp, u'name': u'P1', u'value': u'prop1', u'channels': []}, {u'owner': testp, u'name': u'P2', u'value': u'prop2', u'channels': []}], u'tags': []}], u'owner': testp, u'name': u'P2', u'value': None}

#     Tags
T1_empty = '{"name": "T1", "owner": "' + testt + '"}'
T1_empty_r = {u'owner': testt, u'name': u'T1', u'channels': []}
t1_empty = '{"name": "t1", "owner": "' + testt + '"}'
T1p_empty = '{"name": "T1", "owner": "' + testp + '"}'
T1p_empty_r = {u'owner': testp, u'name': u'T1', u'channels': []}
T2_empty = '{"name": "T2", "owner": "' + testt + '"}'
T2p_empty = '{"name": "T2", "owner": "' + testp + '"}'
T3_empty = '{"name": "T33", "owner": "' + testt + '"}'
T4_empty = '{"name": "T44", "owner": "' + testt + '"}'

Ts1p_empty = '[' + T1p_empty + ']'
Ts12p_empty = '[' + T1p_empty + ', ' + T2p_empty + ']'
Ts12_empty_xml = "<tags><tag name=\"T1\" owner=\"testt\"/><tag name=\"T2\" owner=\"testt\"/></tags>"
Ts12_empty = '[' + T1_empty + ', ' + T2_empty + ']'
Ts12_empty_r = [{u'owner': testt, u'name': u'T1'}, {u'owner': testt, u'name': u'T2'}]
Ts12p_empty_r = [{u'owner': testp, u'name': u'T1'}, {u'owner': testp, u'name': u'T2'}]
Ts1234_empty = '[' + T1_empty + ', ' + T2_empty + ', ' + T3_empty + ', ' + T4_empty + ']'

T1_C1 = '{"name": "T1", "owner": "' + testt + '", "channels": [{"name": "C1", "tags": [{"name": "T1"}]}]}'
T1_C1_r = {u'channels': [{u'owner': testc, u'name': u'C1', u'tags': [{u'owner': testt, u'channels': [], u'name': u'T1'}], u'properties': []}], u'owner': testt, u'name': u'T1'}
t1_C1 = '{"name": "t1", "owner": "' + testt + '", "channels": [{"name": "C1", "owner": "' + testc + '", "tags": [{"name": "t1"}]}]}'
T1_C2 = '{"name": "T1", "owner": "' + testt + '", "channels": [{"name": "C2", "owner": "' + testc + '", "tags": [{"name": "T1"}]}]}'
T1_C2_r = {u'channels': [{u'owner': testc, u'name': u'C2', u'tags': [{u'owner': testt, u'channels': [], u'name': u'T1'}], u'properties': []}], u'owner': testt, u'name': u'T1'}
T1_C12 = '{"name": "T1", "owner": "' + testt + '", "channels": [{"name": "C1", "owner": "' + testc + '", "tags": [{"name": "T1"}]}, {"name": "C2", "owner": "' + testc + '", "tags": [{"name": "T1"}]}]}'
T1_C3 = '{"name": "T1", "owner": "' + testt + '", "channels": [{"name": "C3", "tags": [{"name": "T1"}]}]}'
T2_C2 = '{"name": "T2", "owner": "' + testt + '", "channels": [{"name": "C2", "owner": "' + testc + '", "tags": [{"name": "T2"}]}]}'
T2_C23 = '{"name": "T2", "owner": "' + testt + '", "channels": [{"name": "C2", "tags": [{"name": "T2"}]}, {"name": "C3", "tags": [{"name": "T2"}]}]}'
T2_C23_r = {u'channels': [{u'owner': testc, u'name': u'C2', u'properties': [], u'tags': [{u'owner': testt, u'channels': [], u'name': u'T1'}, {u'owner': testt, u'channels': [], u'name': u'T2'}]}, {u'owner': testc, u'name': u'C3', u'properties': [], u'tags': [{u'owner': testt, u'channels': [], u'name': u'T2'}]}], u'owner': testt, u'name': u'T2'}
t2_C23 = '{"name": "t2", "owner": "' + testt + '", "channels": [{"name": "C2", "tags": [{"name": "t2"}]}, {"name": "C3", "tags": [{"name": "t2"}]}]}'
T3_C1 = '{"name": "T33", "owner": "' + testt + '", "channels": [{"name": "C1", "tags": [{"name": "T33"}]}]}'
T2_C4 = '{"name": "T2", "owner": "' + testt + '", "channels": [{"name": "C4", "tags": [{"name": "T2"}]}]}'


#############################################################################################
# Generic action and check functions
#############################################################################################

def ordered(obj):
    if isinstance(obj, dict):
        return sorted((k, ordered(v)) for k, v in obj.items())
    if isinstance(obj, list):
        return sorted(ordered(x) for x in obj)
    else:
        return obj

def doGetJSON(self, conn, g_url, g_result, g_resp):
    response = conn_none.request_get(g_url, headers=copy(jsonheader))
    self.assertEqual(`g_resp`, response[u'headers']['status'],
    'unexpected return code for get operation - expected ' + `g_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    if (response[u'headers']['status'] != '404'):
        j = JSONDecoder().decode(response[u'body'])
        parsed_j = ordered(j)
        r = ordered(g_result)
        self.assertEqual(parsed_j, r,
        'unexpected result of get operation - expected:\n' + `r` + '\nreceived:\n' + `parsed_j`)

def doPostAndGetJSON(self, conn, p_url, p_body, p_resp, g_url, g_result, g_resp):
    response = conn.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    sleep(1)
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
    sleep(1)
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

def doPostAndFailPlain(self, p_url, p_body):
    p_resp = 302
    response = conn_tag_plain.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_prop_plain.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_chan_plain.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_admin_plain.request_post(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])

def doPutAndFailPlain(self, p_url, p_body):
    p_resp = 302
    response = conn_tag_plain.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_prop_plain.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_chan_plain.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_admin_plain.request_put(p_url, headers=copy(jsonheader), body=p_body)
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])

def doDeleteAndFailPlain(self, p_url):
    p_resp = 302
    response = conn_tag_plain.request_delete(p_url, headers=copy(jsonheader))
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_prop_plain.request_delete(p_url, headers=copy(jsonheader))
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_chan_plain.request_delete(p_url, headers=copy(jsonheader))
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])
    response = conn_admin_plain.request_delete(p_url, headers=copy(jsonheader))
    self.assertEqual(`p_resp`, response[u'headers']['status'],
    'unexpected return code for post operation - expected ' + `p_resp` + ', received ' + response[u'headers']['status'] + ', message body:\n' + response[u'body'])


#############################################################################################
# Test .../tags/<name> PUT and GET   simple version, no channels
#############################################################################################
class PutOneTag(unittest.TestCase):
    """Test PUT and GET on the .../tags/<name> target - without channels in the payload"""
    def setUp(self):
        self.T1 = 'resources/tags/T1'

    def test_AuthorizedAsTag(self):
        doPutAndGetJSON(self, conn_tag, self.T1, T1_empty, 200, self.T1, T1_empty_r, 200)

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

    def test_AuthorizedAsTag(self):
        doPutAndGetJSON(self, conn_tag, self.t, Ts12_empty, 200, self.t, Ts12_empty_r, 200)

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T2, headers=copy(jsonheader))


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
        self.T1 = 'resources/tags/T1?withChannels=true'
        self.T2 = 'resources/tags/T2?withChannels=true'
        self.T3 = 'resources/tags/T3?withChannels=true'
        response = conn_admin.request_put(self.C1, headers=copy(jsonheader), body=C1_empty)
        response = conn_admin.request_put(self.C2, headers=copy(jsonheader), body=C2_empty)
        response = conn_admin.request_put(self.C3, headers=copy(jsonheader), body=C3_empty)
        response = conn_admin.request_put(self.T1, headers=copy(jsonheader), body=T1_C12)
        response = conn_admin.request_put(self.T2, headers=copy(jsonheader), body=T2_C2)

    def test_Unauthorized(self):
        doPostAndFailJSON(self, conn_none, self.T2, T2_C23, 302)
    def test_UnauthorizedSecure(self):
        doPostAndFailJSON(self, conn_none_secure, self.T2, T2_C23, 401)

# Add a channel, test through .../channels GET and .../tags/T2 GET
    def test_AuthorizedAsTag(self):
        doPostAndGetJSON(self, conn_tag, self.T2, T2_C23, 200, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsProp(self):
        doPostAndGetJSON(self, conn_prop, self.T2, T2_C23, 200, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsChan(self):
        doPostAndGetJSON(self, conn_chan, self.T2, T2_C23, 200, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)
    def test_AuthorizedAsAdmin(self):
        doPostAndGetJSON(self, conn_admin, self.T2, T2_C23, 200, self.c, Cs123_1T1_2T12_3T2_r, 200)
        doGetJSON(self, conn_tag, self.T2, T2_C23_r, 200)

# Non-existing tag
    def test_AuthorizedAsTagNonexTag(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T3, T3_C1, 404, "A tag named 'T3' does not exist")

# Non-existing channel
    def test_AuthorizedAsTagNonexChannel(self):
        doPostAndFailMessageJSON(self, conn_tag, self.T2, T2_C4, 404, "DocumentMissingException[[channelfinder][-1] [channel][C4]: document missing]")

    def tearDown(self):
        response = conn_admin.request_delete(self.T1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.T2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C1, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C2, headers=copy(jsonheader))
        response = conn_admin.request_delete(self.C3, headers=copy(jsonheader))


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
