"""
    Copyright (C) 2008 Benjamin O'Steen

    This file is part of python-fedoracommons.

    python-fedoracommons is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    python-fedoracommons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with python-fedoracommons.  If not, see <http://www.gnu.org/licenses/>.
"""

__license__ = 'GPL http://www.gnu.org/licenses/gpl.txt'
__author__ = "Benjamin O'Steen <bosteen@gmail.com>"
__version__ = '0.2'

import httplib2
import urlparse
import urllib
import base64
from base64 import encodestring

from mimeTypes import *

import mimetypes

from cStringIO import StringIO

class ConnectionError(Exception):
    def __str__(self):
        return "Connection failed"

class Connection:
    def __init__(self, base_url, username=None, password=None):
        self.base_url = base_url
        self.username = username
        m = mimeTypes()
        self.mimetypes = m.getDictionary()
        
        self.url = urlparse.urlparse(base_url)
        
        (scheme, netloc, path, query, fragment) = urlparse.urlsplit(base_url)
            
        self.scheme = scheme
        self.host = netloc
        self.path = path
        
        # Create Http class with support for Digest HTTP Authentication, if necessary
        self.h = httplib2.Http(".cache", disable_ssl_certificate_validation=True)
        self.h.follow_all_redirects = True
        if username and password:
            self.h.add_credentials(username, password)
    
    def request_get(self, resource, args = None, headers={}):
        headers = headers or {}
        return self.request(resource, "get", args, headers=headers)
        
    def request_delete(self, resource, args = None, headers={}):
        headers = headers or {}
        return self.request(resource, "delete", args, headers=headers)
        
    def request_head(self, resource, args = None, headers={}):
        headers = headers or {}
        return self.request(resource, "head", args, headers=headers)
        
    def request_post(self, resource, args = None, body = None, filename=None, headers={}):
        headers = headers or {}
        return self.request(resource, "post", args , body = body, filename=filename, headers=headers)
        
    def request_put(self, resource, args = None, body = None, filename=None, headers={}):
        headers = headers or {}
        return self.request(resource, "put", args , body = body, filename=filename, headers=headers)
        
    def get_content_type(self, filename):
        extension = filename.split('.')[-1]
        guessed_mimetype = self.mimetypes.get(extension, mimetypes.guess_type(filename)[0])
        return guessed_mimetype or 'application/octet-stream'
        
    def request(self, resource, method = "get", args = None, body = None, filename=None, headers={}):
        """
        Modified. Filename represents the actual file object.
        """
        params = None
        path = resource
        if 'User-Agent' not in headers:
            headers['User-Agent'] = 'restful_lib.py/' + __version__
            # add httplib2 info # + ' httplib2.py/' + version 
        
        BOUNDARY = u'00hoYUXOnLD5RQ8SKGYVgLLt64jejnMwtO7q8XE1'
        CRLF = u'\r\n'
        
        if filename:
            #fn = open(filename ,'r')
            #chunks = fn.read()
            #fn.close()
            
            content_type = self.get_content_type(filename.name)
            # Attempt to find the Mimetype
            headers['Content-Type']='multipart/form-data; boundary='+BOUNDARY

            encode_string = StringIO()

            if args:
              for key, val in args.items():
                encode_string.write(u'--' + BOUNDARY + CRLF)
                encode_string.write(u'Content-Disposition: form-data; name="%s"' % (key))
                encode_string.write(CRLF)
                encode_string.write(CRLF)
                encode_string.write(val)
                encode_string.write(CRLF)

            #encode_string.write(CRLF)
            encode_string.write(u'--' + BOUNDARY + CRLF)
            encode_string.write(u'Content-Disposition: form-data; name="file"; filename="%s"' % filename.name)
            encode_string.write(CRLF)
            encode_string.write(u'Content-Type: %s' % content_type + CRLF)
            encode_string.write(CRLF)
            encode_string.write(filename.read())
            encode_string.write(CRLF)
            encode_string.write(u'--' + BOUNDARY + u'--' + CRLF)

            filename.close()
            
            body = encode_string.getvalue()
            headers['Content-Length'] = str(len(body))
        elif body:
            if 'Content-Type' not in headers:
                headers['Content-Type']='text/xml'
            headers['Content-Length'] = str(len(body))        
        else:
            if 'Content-Length' in headers:
                del headers['Content-Length']
            
            headers['Content-Type']='text/plain'
            
            if args:
                if method == "get":
                    path += u"?" + urllib.urlencode(args)
                elif method == "put" or method == "post":
                    headers['Content-Type']='application/x-www-form-urlencoded'
                    body = urllib.urlencode(args)

            
        request_path = []
        # Normalise the / in the url path
        if self.path != "/":
            if self.path.endswith('/'):
                request_path.append(self.path[:-1])
            else:
                request_path.append(self.path)
            if path.startswith('/'):
                request_path.append(path[1:])
            else:
                request_path.append(path)
        
        resp, content = self.h.request(u"%s://%s%s" % (self.scheme, self.host, u'/'.join(request_path)), method.upper(), body=body, headers=headers )
        # TODO trust the return encoding type in the decode?
        return {u'headers':resp, u'body':content.decode('UTF-8')}
