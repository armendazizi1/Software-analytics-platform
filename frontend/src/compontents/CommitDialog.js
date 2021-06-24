import React from 'react';
// import Button from '@material-ui/core/Button';
import { Modal, Table,Button, Spinner } from 'react-bootstrap';
import { FaArrowCircleUp, FaArrowCircleDown, FaArrowCircleRight } from "react-icons/fa";
import SimpleCard from './SimpleCard'
import SimpleService from '../services/SimpleService'

export default class  CommitDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      commitId : this.props.commitId,
      projectName : this.props.projectName,
      commitData : [],
      gotData : false
    }

    this.getNextCommit = this.getNextCommit.bind(this);
  }

  // componentDidMount(){
  //   this.getNextCommit(this.state.commitId)
  // }
  
  static getDerivedStateFromProps(nextProps, prevState) {
    return {
     commitId: nextProps.commitId,
     projectName: nextProps.projectName,
     commitData: nextProps.commitData,
     gotData: nextProps.gotData,
    };
   }


   getNextCommit(e){
    // console.log("this is eeeeee",e)
    var commitId= e;
    this.setState({gotData:false});
    // console.log('this is gotData2323',this.state.gotData)
    var commit2={};
    // console.log("commitID",commitId, ' project name', this.state.projectName)
    SimpleService.getCommitData(commitId,this.state.projectName).then((response)=>{
      // console.log("Response from commitData IN COMITDIALOG",response.data)
      var commitData2 = response.data.left;
      var commitDataMetrics = response.data.right.left;
      var commitDataMetricsComparison = response.data.right.right;

      commit2.cbo = {
        metric:commitDataMetrics.couplingBetweenObjects.toFixed(2),
        comparison: commitDataMetricsComparison.couplingBetweenObjects.toFixed(2)
      };
      commit2.lcom = {
        metric:commitDataMetrics.lackCohesionMethods.toFixed(2),
        comparison: commitDataMetricsComparison.lackCohesionMethods.toFixed(2),
      };
      commit2.loc = {
        metric:commitDataMetrics.linesOfCode.toFixed(2),
        comparison: commitDataMetricsComparison.linesOfCode.toFixed(2)
      };
      commit2.wmc = {
        metric:commitDataMetrics.weightMethodClass.toFixed(2),
        comparison: commitDataMetricsComparison.weightMethodClass.toFixed(2)
      };

      commit2.id = commitData2.id;
      commit2.created_at = new Date(commitData2.date)
      // console.log("created_at",commit2.created_at)
      commit2.author = commitData2.author;
      commit2.body = commitData2.body;
      commit2.files_added = commitData2.filesAdded ? commitData2.filesAdded : [];
      commit2.files_removed = commitData2.filesRemoved ? commitData2.filesRemoved : [];
      commit2.files_modified = commitData2.filesModified ? commitData2.filesModified : [];
      commit2.files_renamed = commitData2.filesRenamed ? commitData2.filesRenamed : [];
      commit2.note = commitData2.note;
      commit2.message = commitData2.message;
      commit2.next = commitData2.next;
      commit2.clossesIssue = commitData2.issueNumber?commitData2.issueNumber:null;
      // console.log("this is commit2",commit2)
      this.setState({commitId,commitData: commit2, gotData:true})
    })

    return [this.state.commitData]
   }

  render() {
    // console.log('this is commitId,', this.state.commitId)
    // console.log("gotData props ",this.state.gotData)
    // console.log("gotData ",this.props.gotData)
    var commitId = this.state.commitId;
    var projectName = this.state.projectName;
    var commitData = this.state.commitData;
    var gotData = this.state.gotData;
    // console.log("this is commitData ",commitData)
  return (
    <>
      
      <Modal
        size="lg"
        aria-labelledby="contained-modal-title-vcenter"
        centered show={this.props.show} onHide={this.props.handleShow} animation={true}>
        <Modal.Header style={{backgroundColor:'grey', color:'white'}} closeButton>
          <Modal.Title>Commit {commitId}</Modal.Title>
        </Modal.Header>
        {gotData?
        <Modal.Body>
          <SimpleCard data={commitData} date={commitData.created_at} projectName={projectName}/>
        {/* next:<Button value = {commitData.next} onClick={(val)=>this.getNextCommit(val.target.value)}>{commitData.next}</Button> */}
        {/* next:<Button value = {commitData.next} onClick={(val)=>this.props.getNextCommit(val.target.value)}>{commitData.next}</Button> */}
          <hr/>
          {commitData.inducingBugFixedBy.length >0 &&
          <ul style={{listStyleType: 'none'}}>
            <li>Induces bug that is then fixed by:</li>
            {commitData.inducingBugFixedBy.map((commitId, i) => {
              return <li key={i}> 
              {<Button variant="link" value = {commitId} onClick={(val)=>this.props.getNextCommit(val.target.value)}>{commitId}</Button>}
               </li>
            })}
          </ul>
          }
          {commitData.resolvingBugIntroducedBy.length > 0 && 
          <ul style={{listStyleType: 'none'}}>
              {commitData.label === 'resolvesBug'&& <li>Fixes bug that is induced by:</li> }
              {commitData.label === 'inducingBug'&& <li>Induces bug that is then fixed by:</li> }
            {commitData.resolvingBugIntroducedBy.map((commitId, i) => {
              return <li key={i}> 
              {<Button variant="link" value = {commitId} onClick={(val)=>this.props.getNextCommit(val.target.value)}>{commitId}</Button>}
               </li>
            })}
          </ul>
          }

          <hr />

          <ul>
            {commitData.files_modified.map((file, i) => {
              return <li key={i}>M {file}</li>
            })}
          </ul>

          <ul>
            {commitData.files_added.map((file, i) => {
              return <li key={i}>A {file}</li>
            })}
          </ul>

          <ul>
            {commitData.files_removed.map((file, i) => {
              return <li key={i}>D {file}</li>
            })}
          </ul>

          {/* <ul>
            {Object.values(commitData.files_renamed).map((file, i) => {
              return <li key={i}>R {file}</li>
            })}
          </ul> */}

          Diff: <a href={"https://github.com/" + projectName + "/commit/" + commitData.id} target="_blank" rel="noopener noreferrer">{"https://github.com/" + projectName + "/commit/" + commitData.id} </a>
          <hr />

          <Table style={{ textAlign: 'center' }} striped bordered hover>
            <thead >
              <tr >
                <th>Metric</th>
                <th>Value</th>
                <th>Improvement</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>CBO</td>
          <td>{commitData.cbo.metric}</td>
            {commitData.cbo.comparison === 0.00? 
            <td><FaArrowCircleRight color='grey' />  {commitData.cbo.comparison}%</td>:
            commitData.cbo.comparison < 0.00? 
            <td><FaArrowCircleDown color='red' /> {commitData.cbo.comparison}%</td>:
            <td><FaArrowCircleUp color='green' />  {commitData.cbo.comparison}%</td>
            }
              </tr>
              <tr>
                <td>LOC</td>
          <td>{commitData.loc.metric}</td>
          {commitData.loc.comparison === 0.00? 
            <td><FaArrowCircleRight color='grey' />  {commitData.loc.comparison}%</td>:
            commitData.loc.comparison < 0.00? 
            <td><FaArrowCircleDown color='red' /> {commitData.loc.comparison}%</td>:
            <td><FaArrowCircleUp color='green' />  {commitData.loc.comparison}%</td>
            }
              </tr>
              <tr>
                <td>WMC</td>
                <td>{commitData.wmc.metric}</td>
                {commitData.wmc.comparison === 0.00? 
            <td><FaArrowCircleRight color='grey' />  {commitData.wmc.comparison}%</td>:
            commitData.wmc.comparison < 0.00? 
            <td><FaArrowCircleDown color='red' /> {commitData.wmc.comparison}%</td>:
            <td><FaArrowCircleUp color='green' />  {commitData.wmc.comparison}%</td>
            }
              </tr>
              <tr>
                <td>LCOM</td>
                <td>{commitData.lcom.metric}</td>
                {commitData.lcom.comparison === 0.00? 
            <td><FaArrowCircleRight color='grey' /> {commitData.lcom.comparison}%</td>:
            commitData.lcom.comparison < 0.00? 
            <td><FaArrowCircleDown color='red' /> {commitData.lcom.comparison}%</td>:
            <td><FaArrowCircleUp color='green' />  {commitData.lcom.comparison}%</td>
            }
              </tr>
            </tbody>
          </Table>
        </Modal.Body>
                  : <Modal.Body> <div style={{display:'flex'}}><Spinner animation="border" variant="primary" />   <h5 style={{marginLeft:'10px'}}>  Calculating metrics...</h5></div> </Modal.Body>}
        <Modal.Footer>
          <Button variant="secondary" onClick={this.props.handleShow}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}
}