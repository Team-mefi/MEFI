import { localAxios } from "@/util/http-commons";
const local = localAxios();

async function teamSelect(param, success, fail){
    local.defaults.headers["Authorization"] = 'Bearer ' + localStorage.getItem("accessToken");
    await local.get(`api/team`, param).then(success).catch(fail);
}
async function teamCreate(param, success, fail){
    local.defaults.headers["Authorization"] = 'Bearer ' + localStorage.getItem("accessToken");
    await local.post(`api/team`, param).then(success).catch(fail);
}

export { teamSelect, teamCreate };