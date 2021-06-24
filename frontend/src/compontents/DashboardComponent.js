import React from 'react';
import SimpleService from '../services/SimpleService'
import { Button, Container, Row, Col, Navbar, Nav, NavDropdown, Form, FormControl, Spinner } from 'react-bootstrap';
import CommitChart from './CommitChart'
import IssueDatagrid from './issueDatagrid'
import ContributorTable from './ContributorTable'
import PRTable from './PRTable'
import FileContributorTable from './FileContributorTable'

import JITPredictionTable from './JITPredictionTable'

import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import History from '@material-ui/icons/History';
import PersonOutlineIcon from '@material-ui/icons/PersonOutline';
import FileCopyOutlinedIcon from '@material-ui/icons/FileCopyOutlined';
import SettingsOutlinedIcon from '@material-ui/icons/SettingsOutlined';
import GitHubIcon from '@material-ui/icons/GitHub';

class DashboardComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      gotData: false,
      searchInput: "",
      selectedProject: "",
      projects: ["armendazizi1/sa-demo"],
      linkedCommits: {},
      filesMatrix: [],
      contributors: [],
      pullRequests: [],
      jitPrediction:[],
      precRec:{},
      commitPredictions:[],
      commits: [{
        id: 0,
        created_at: new Date(),
      },],
      issues: []
    }
    this.fetchData = this.fetchData.bind(this);
    this.handleInputChange = this.handleInputChange.bind(this);
    this.populateCommits = this.populateCommits.bind(this);
    this.populateIssues = this.populateIssues.bind(this);
    this.populateLinkedCommits = this.populateLinkedCommits.bind(this);
    this.populateJITPrediction = this.populateJITPrediction.bind(this);
  }

  componentDidMount() {

    SimpleService.getRepoNames().then((response) => {
      console.log("REPONAMES ", response);
      let projects = this.state.projects;
      response.data.forEach((project) => {
        if (!projects.includes(project)) {
          projects.push(project);
        }
      });

      if (projects.length > 0) {
        this.fetchData(projects[0])
        this.setState({ projects, selectedProject: projects[0] })
      }
    })
  }

  fetchData(projectToFetch) {
    this.setState({ hasProject: true });
    if (projectToFetch !== this.state.selectedProject) {
      this.setState({ gotData: false });
      SimpleService.getData(projectToFetch).then((response) => {
        console.log(response);
        let projects = this.state.projects;
        if (!projects.includes(projectToFetch)) {
          projects.push(projectToFetch);
        }


        this.populateCommits(response.data);
        this.populateIssues(response.data);
        this.populateLinkedCommits(response.data.coupling.commitToIssues);
        this.populateJITPrediction(response.data.predictedCommits);


        let projectName = response.data.name;
        let matrix = response.data.matrix.percentages
        let contributors = response.data.people
        let prs = response.data.peopleLogin

        let precision = response.data.precRec.left.toFixed(2);
        let recall = response.data.precRec.right.toFixed(2);
        let precRec = {prec:precision, rec: recall}

        this.setState({ precRec, projects, selectedProject: projectName, filesMatrix: matrix, contributors: contributors, pullRequests: prs })
        this.setState({ gotData: true})
      });
    }
  }

  populateLinkedCommits(data) {
    this.setState({ linkedCommits: data })

  }

  populateIssues(data) {
    let issues = [];
    let issue = {};
    data.issues.forEach((i) => {
      issue = {};
      issue.id = i.first;
      issue.number = i.second;
      issue.state = i.fourth;
      issue.title = i.third;
      issues.push(issue);

    });

    this.setState({ issues });
  }

  populateCommits(data) {
    let commits = []
    let commit = {}
    data.commits.forEach((i) => {
      commit = {};
      commit.id = i.first;
      commit.created_at = new Date(i.second);
      commit.state = i.third;

      commits.push(commit);
    })
    this.setState({ commits });
  }

  populateJITPrediction(predictedCommits){
  
    let lastTenCommits = this.state.commits.slice(0,10)
    // console.log("last ten commits",lastTenCommits)
    let lastTen =[]
    let commit = {}
    lastTenCommits.forEach((i,index)=>{
      commit = {};
      commit.id = i.id;
      commit.created_at = i.created_at;
      commit.prediction = predictedCommits[index].left;
      commit.probability = (predictedCommits[index].right * 100).toFixed(0);

      let label = 'clean';
      if(i.state){
        let labels = i.state
        // console.log("these are labels ", labels)
      if(labels.includes("inducingBug")){
          label = 'buggy'
        }

      }
      commit.state = label
      lastTen.push(commit)
    })
    // console.log(lastTen)
    this.setState({jitPrediction:lastTen})
  }

  handleInputChange(e) {
    this.setState({ searchInput: e.target.value });
  }

  handleUpdateProject = () => {
    this.setState({ gotData: false })
    let currentProject = this.state.selectedProject;
    SimpleService.updateProject(currentProject).then((response) => {
      console.log("update ", response);

      this.populateCommits(response.data);
      this.populateIssues(response.data);
      this.populateLinkedCommits(response.data.coupling.commitToIssues);
      this.populateJITPrediction(response.data.predictedCommits);

      let matrix = response.data.matrix.percentages
      let contributors = response.data.people
      let prs = response.data.peopleLogin
      let precision = response.data.precRec.left.toFixed(2);
      let recall = response.data.precRec.right.toFixed(2);
      let precRec = {prec:precision, rec: recall}
 

      this.setState({precRec, filesMatrix: matrix, contributors: contributors, pullRequests: prs })


      this.setState({ gotData: true })
    });
  }

  render() {
    return (
      <Container style={{ margin: '0px', padding: 0 }} fluid>
        <Row>
          <Col className="header"> <Navbar collapseOnSelect expand="lg" bg="dark" variant="dark">
            <Navbar.Brand href="#home">Software Analytics</Navbar.Brand>
            <Navbar.Toggle aria-controls="responsive-navbar-nav" />
            <Navbar.Collapse id="responsive-navbar-nav">
              <Nav className="mr-auto">
                <NavDropdown title="Projects" id="collasible-nav-dropdown"
                  onSelect={(event, eventKey) => {
                    // console.log("this is EVENT ", event);
                    this.setState({ selectedProject: event });
                    this.fetchData(event);
                  }}>
                  {this.state.projects.map((i) => {
                    return <NavDropdown.Item key={i} eventKey={i}>{i}</NavDropdown.Item>
                  })}
                </NavDropdown>
                <Button variant="outline-light" onClick={this.handleUpdateProject}>Update</Button>
              </Nav>
              <Nav className="mr-auto">
                <Button variant="dark" disabled size="lg">
                <GitHubIcon/> {this.state.selectedProject}
                </Button>
              </Nav>
              <Form inline>
                <FormControl type="text" placeholder="Search Repository" onChange={this.handleInputChange} className="mr-sm-2" />
                <Button variant="outline-success" onClick={() => this.fetchData(this.state.searchInput)}>Search</Button>
              </Form>
            </Navbar.Collapse>
          </Navbar> </Col>
        </Row>
        {
          this.state.gotData ?
            <div>

              <Row>
                <Col style={{ margin: '20px', textAlign: 'center' }}>
                  <h5><History />  <strong>{this.state.commits.length}</strong> commits </h5>

                  <CommitChart projectName={this.state.selectedProject} data={this.state.commits} />
                </Col>
                <Col style={{ margin: '20px', textAlign: 'center' }}>
                  <h5> <ErrorOutlineIcon />  <strong>{this.state.issues.length}</strong> issues</h5>
                  <IssueDatagrid projectName={this.state.selectedProject} issues={this.state.issues} linkedCommits={this.state.linkedCommits} />
                </Col>
              </Row>
              <Row>
              <Col xs={5} style={{ margin: '20px 0px 40px 20px', textAlign: 'left' }} >
                  <h5 style={{ textAlign: 'center' }}> <PersonOutlineIcon />  <strong>{this.state.contributors.length}</strong> contributors</h5>
                  <ContributorTable data={this.state.contributors} /></Col>
              <Col style={{ margin: '20px 20px 40px 0px', textAlign: 'left' }}>
              <h5 style={{ textAlign: 'center' }}> <SettingsOutlinedIcon /> JIT defect prediction with <strong>{this.state.precRec.prec}%</strong> precision &  <strong>{this.state.precRec.rec}%</strong> recall</h5>
                <JITPredictionTable projectName={this.state.selectedProject} data = {this.state.jitPrediction} />
                </Col>
              </Row>
              <Row>
                {/* <Col  style={{margin:'20px',textAlign:'center'}}>
                
                <Row> */}
         
                <Col xs={5}  style={{ margin: '20px 0px 40px 20px', textAlign: 'left' }} >
                  <h5 style={{ textAlign: 'center' }}> <PersonOutlineIcon />  <strong>{this.state.pullRequests.length}</strong> developers involved in PRs </h5>
                  <PRTable data={this.state.pullRequests} /></Col>

                <Col style={{ margin: '20px 20px 40px 0px', textAlign: 'left' }}>
                  <h5 style={{ textAlign: 'center' }}> <FileCopyOutlinedIcon />  <strong>{this.state.filesMatrix.length}</strong> files</h5>
                  <FileContributorTable data={this.state.filesMatrix} />
                </Col>
              </Row>
     
            </div>
            :

            <Row>
              <Col></Col>
              <Col>
                <Button variant="primary" disabled>
                  <Spinner
                    as="span"
                    animation="grow"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                  />
                    Loading...
                  </Button>

              </Col>

            </Row>
        }
      </Container>
    );
  }
}

export default DashboardComponent;
