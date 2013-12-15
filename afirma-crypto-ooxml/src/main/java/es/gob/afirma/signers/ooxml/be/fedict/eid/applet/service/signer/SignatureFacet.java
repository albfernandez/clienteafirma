/*
 * eID Applet Project.
 * Copyright (C) 2009 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see
 * http://www.gnu.org/licenses/.
 */

/*
 * Copyright (C) 2008-2009 FedICT.
 * This file is part of the eID Applet Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.gob.afirma.signers.ooxml.be.fedict.eid.applet.service.signer;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** JSR105 Signature Facet interface.
 * @author fcorneli */
public interface SignatureFacet {

    /** This method is being invoked by the XML signature service engine during
     * pre-sign phase. Via this method a signature facet implementation can add
     * signature facets to an XML signature.
     * @param signatureFactory
     * @param document
     * @param signatureId
     * @param references
     * @param objects
     * @param signatureComments Taz&oacute;n para la firma
     * @param address1 Direcci&oacute;n donde se ha realizado la firma (campo 1)
     * @param address2 Direcci&oacute;n donde se ha realizado la firma (campo 2)
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException */
    void preSign(byte[] ooXmlDocument,
    		     XMLSignatureFactory signatureFactory,
                 Document document,
                 String signatureId,
                 List<Reference> references,
                 List<XMLObject> objects,
                 String signatureComments,
                 String address1,
                 String address2) throws NoSuchAlgorithmException,
                                         InvalidAlgorithmParameterException,
                                         IOException,
                                         ParserConfigurationException,
                                         SAXException,
                                         TransformerException;
}
