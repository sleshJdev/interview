public class DwhMdmClient {
    private static final String VERIFICATION_ORGANIZATION_IN_MDM_LIST = "xml/data/V_MDM_CHECK_ICDB_UR";
    private static final String FILTER_FOR_CHECK_ORGANIZATION = "?filter=GOLD_ID=%s&DICT_NAME<>INTERFAX_PEOPLE";
    private final String dwhMdmUrl;
    private final RestTemplate restTemplate;
    public DwhMdmClient(@Value("${mdm.dwh-api-client.server.url}") String dwhMdmUrl,
                        RestTemplate restTemplate) {
        this.dwhMdmUrl = dwhMdmUrl;
        this.restTemplate = restTemplate;
    }
    public boolean isOrganizationInTerroristList(String mdmId) {
        Rows rows = checkIcdbUr(String.format(FILTER_FOR_CHECK_ORGANIZATION, mdmId));
        Optional<Rows.Row> row = rows.getRow().stream()
                .filter(rowIns -> findTerrorist(rowIns)
                        .isPresent()).findFirst();
        return row.isPresent();
    }
    private Optional<Rows.Row.AttrValue> findTerrorist(Rows.Row row) {
        return row.getAttrValue().stream()
                .filter(attrV -> ("CHECK_RESULT".equals(attrV.getAttrName()) && "T".equals(attrV.getValue())))
                .findFirst();
    }
    public Rows checkIcdbUr(String filter) {
        return getResponseDwhApi(VERIFICATION_ORGANIZATION_IN_MDM_LIST + filter, Rows.class);
    }
    private <T> T getResponseDwhApi(String apiMethod, Class<T> responseType) {
        if (dwhMdmUrl == null || dwhMdmUrl.isEmpty()) {
            throw new MdmException("Не указан базовый адрес dwhМДМ mdm.server.dwh-url");
        }
        String url = dwhMdmUrl + apiMethod;
        T response = restTemplate.getForObject(url, responseType);
        log.debug("url : {}; response : {}", url, response);
        return response;
    }
}
