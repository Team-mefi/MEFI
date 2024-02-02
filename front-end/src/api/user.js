import { localAxios } from "@/util/http-commons";
const local = localAxios();

async function userLogin(param, success, fail){
    await local.post(`/api/users/login`, param).then(success).catch(fail);
}
async function userSignup(param, success, fail){
    await local.post(`/api/users`, param).then(success).catch(fail);
}
export { userLogin, userSignup };