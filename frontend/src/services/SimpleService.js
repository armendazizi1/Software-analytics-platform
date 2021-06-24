import axios from 'axios';

const URL= 'http://localhost:8080/'

// Uncomment this when on VM and comment the previous URL

//const URL= 'http://195.176.181.137:8080/'

class SimpleService {

  getData(url) {
    let URL2= 'repoAnalysis?repoUrl=';
    URL2 = URL + URL2 + url
    return axios.get(URL2)
    //   ,{
    //   onDownloadProgress: (progressEvent) => {
    //     console.log("progress",progressEvent)
    // }
    // })
  }


  updateProject(url){
    let URL2= 'repoUpdate?repoUrl=';
    URL2 = URL + URL2 + url
    return axios.get(URL2)
    //   ,{
  }


  getRepoNames() {
    let URL2= URL + 'repoNames';
    return axios.get(URL2)
  }

  getCommitData(commitId,projectName) {
    let URL2 =  URL + 'commit/' + commitId + '?projectName='+projectName;
    return axios.get(URL2)
  }

  getIssueData(issueId,projectName) {
    let URL2=  URL + 'issue/' + issueId + '?projectName='+projectName;
    return axios.get(URL2)
  }

  postData() {
    const json = JSON.stringify({ answer: 50 });
    const res = axios.post('https://httpbin.org/post', json, {
      headers: {
        // Overwrite Axios's automatically set Content-Type
        'Content-Type': 'application/json'
      }
    });
    return res;

  }
}

export default new SimpleService();