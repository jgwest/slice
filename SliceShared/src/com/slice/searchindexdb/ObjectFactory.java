//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.09.04 at 10:08:59 PM EDT 
//


package com.slice.searchindexdb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.slice.searchindexdb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _QueryIdResult_QNAME = new QName("http://www.slice.com/SearchIndexDB", "QueryIdResult");
    private final static QName _FileIdList_QNAME = new QName("http://www.slice.com/SearchIndexDB", "FileIdList");
    private final static QName _FileId_QNAME = new QName("http://www.slice.com/SearchIndexDB", "FileId");
    private final static QName _QueryIdStatus_QNAME = new QName("http://www.slice.com/SearchIndexDB", "QueryIdStatus");
    private final static QName _NewQueryIdListResponse_QNAME = new QName("http://www.slice.com/SearchIndexDB", "NewQueryIdListResponse");
    private final static QName _NewQueryIdList_QNAME = new QName("http://www.slice.com/SearchIndexDB", "NewQueryIdList");
    private final static QName _LastUpdateInMsecs_QNAME = new QName("http://www.slice.com/SearchIndexDB", "LastUpdateInMsecs");
    private final static QName _IdList_QNAME = new QName("http://www.slice.com/SearchIndexDB", "IdList");
    private final static QName _IdListSize_QNAME = new QName("http://www.slice.com/SearchIndexDB", "IdListSize");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.slice.searchindexdb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link QueryIDResultDTO }
     * 
     */
    public QueryIDResultDTO createQueryIDResultDTO() {
        return new QueryIDResultDTO();
    }

    /**
     * Create an instance of {@link QueryIdStatusDTO }
     * 
     */
    public QueryIdStatusDTO createQueryIdStatusDTO() {
        return new QueryIdStatusDTO();
    }

    /**
     * Create an instance of {@link FileIdListTypeDTO }
     * 
     */
    public FileIdListTypeDTO createFileIdListTypeDTO() {
        return new FileIdListTypeDTO();
    }

    /**
     * Create an instance of {@link NewQueryIdListDTO }
     * 
     */
    public NewQueryIdListDTO createNewQueryIdListDTO() {
        return new NewQueryIdListDTO();
    }

    /**
     * Create an instance of {@link FileIdTypeDTO }
     * 
     */
    public FileIdTypeDTO createFileIdTypeDTO() {
        return new FileIdTypeDTO();
    }

    /**
     * Create an instance of {@link NewQueryIdListResponseDTO }
     * 
     */
    public NewQueryIdListResponseDTO createNewQueryIdListResponseDTO() {
        return new NewQueryIdListResponseDTO();
    }

    /**
     * Create an instance of {@link IdListTypeDTO }
     * 
     */
    public IdListTypeDTO createIdListTypeDTO() {
        return new IdListTypeDTO();
    }

    /**
     * Create an instance of {@link QueryIDResultDTO.QueryIdResultResultDTO }
     * 
     */
    public QueryIDResultDTO.QueryIdResultResultDTO createQueryIDResultDTOQueryIdResultResultDTO() {
        return new QueryIDResultDTO.QueryIdResultResultDTO();
    }

    /**
     * Create an instance of {@link QueryIdStatusDTO.FileIdStatusDTO }
     * 
     */
    public QueryIdStatusDTO.FileIdStatusDTO createQueryIdStatusDTOFileIdStatusDTO() {
        return new QueryIdStatusDTO.FileIdStatusDTO();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryIDResultDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "QueryIdResult")
    public JAXBElement<QueryIDResultDTO> createQueryIdResult(QueryIDResultDTO value) {
        return new JAXBElement<QueryIDResultDTO>(_QueryIdResult_QNAME, QueryIDResultDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileIdListTypeDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "FileIdList")
    public JAXBElement<FileIdListTypeDTO> createFileIdList(FileIdListTypeDTO value) {
        return new JAXBElement<FileIdListTypeDTO>(_FileIdList_QNAME, FileIdListTypeDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileIdTypeDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "FileId")
    public JAXBElement<FileIdTypeDTO> createFileId(FileIdTypeDTO value) {
        return new JAXBElement<FileIdTypeDTO>(_FileId_QNAME, FileIdTypeDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryIdStatusDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "QueryIdStatus")
    public JAXBElement<QueryIdStatusDTO> createQueryIdStatus(QueryIdStatusDTO value) {
        return new JAXBElement<QueryIdStatusDTO>(_QueryIdStatus_QNAME, QueryIdStatusDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NewQueryIdListResponseDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "NewQueryIdListResponse")
    public JAXBElement<NewQueryIdListResponseDTO> createNewQueryIdListResponse(NewQueryIdListResponseDTO value) {
        return new JAXBElement<NewQueryIdListResponseDTO>(_NewQueryIdListResponse_QNAME, NewQueryIdListResponseDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NewQueryIdListDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "NewQueryIdList")
    public JAXBElement<NewQueryIdListDTO> createNewQueryIdList(NewQueryIdListDTO value) {
        return new JAXBElement<NewQueryIdListDTO>(_NewQueryIdList_QNAME, NewQueryIdListDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "LastUpdateInMsecs")
    public JAXBElement<Long> createLastUpdateInMsecs(Long value) {
        return new JAXBElement<Long>(_LastUpdateInMsecs_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdListTypeDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "IdList")
    public JAXBElement<IdListTypeDTO> createIdList(IdListTypeDTO value) {
        return new JAXBElement<IdListTypeDTO>(_IdList_QNAME, IdListTypeDTO.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.slice.com/SearchIndexDB", name = "IdListSize")
    public JAXBElement<Long> createIdListSize(Long value) {
        return new JAXBElement<Long>(_IdListSize_QNAME, Long.class, null, value);
    }

}
