import * as React from 'react';
import { DataGrid } from '@material-ui/data-grid';
import { Button } from 'react-bootstrap';
import CommitDialog from './CommitDialog'
import SimpleService from '../services/SimpleService'

export default class CommitChart extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
        something:'hello',
        gotData:false,
        commits: this.props.data,
        tripeCommits:[],
        commit2:{},
        projectName: this.props.projectName,
        showModal: false,
    }
    this.handleShow = this.handleShow.bind(this);
    this.myFunction = this.myFunction.bind(this);
    this.getNextCommit = this.getNextCommit.bind(this);
  }

  getNextCommit(e){
    // console.log('eeeeeeee',e)
    this.setState({commitId:e})
    this.fetchCommitData(e,this.state.projectName)
  }


  myFunction (id){
    this.handleShow();
    this.setState({gotData:false, commitId:id})
    this.fetchCommitData(id, this.props.projectName)
  }
  
  handleShow() {
    if(this.state.showModal === false){
      this.setState({ gotData: false })
    }
    this.setState({ showModal: !this.state.showModal })
  }

  fetchCommitData(commitId, projectName) {
    this.setState({gotData:false});
    var commit2={};
    SimpleService.getCommitData(commitId,projectName).then((response)=>{
      console.log("Response from commitData",response.data)
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
      commit2.created_at = new Date(commitData2.date.left)
      commit2.author = commitData2.author;
      commit2.body = commitData2.body;
      commit2.files_added = commitData2.difference.added ? commitData2.difference.added : [];
      commit2.files_removed = commitData2.difference.deleted  ? commitData2.difference.deleted  : [];
      commit2.files_modified = commitData2.difference.modified  ? commitData2.difference.modified  : [];
      commit2.files_renamed = commitData2.difference.renamed  ? commitData2.difference.renamed  : [];
      commit2.note = commitData2.note;
      commit2.message = commitData2.message;
      commit2.next = commitData2.next;
      let label = '';
      if(commitData2.labels){
        let labels = commitData2.labels
        if (labels.includes('resolvesBug') && !labels.includes('inducingBug')){
          label = 'resolvesBug'
        }
        else if(labels.includes('inducingBug')){
          label = 'inducingBug'
        }

      }

      commit2.label = label
      commit2.resolvingBugIntroducedBy = commitData2.resolvingBugIntroducedBy? commitData2.resolvingBugIntroducedBy:[];
      commit2.inducingBugFixedBy = commitData2.inducingBugFixedBy? commitData2.inducingBugFixedBy:[];

      commit2.clossesIssue = commitData2.issueNumber?commitData2.issueNumber:null;
      // console.log("this is commit2",commit2)
      this.setState({commit2, gotData:true})
    })

  }

  render() {

    const columns = [
      // { field: 'id', headerName: 'ID', width: 70 },
      { field: 'commitId', headerName: 'Commit id', type: 'string', align: 'left', width: 200, resizable: false, 
      renderCell: (params) => {
        const data = params.data? params.data:params.row;
      
        return <Button onClick={()=> this.myFunction(data.commitId)} variant="link">{data.commitId.slice(0,10)}</Button>
    
      }
    
    },
    { field: 'date', headerName: 'Date', width: 220, valueFormatter:({value}) => value.getDate() + " " + months[value.getMonth()] + " " + value.getFullYear() + " " + value.getHours() + ":"+ (value.getMinutes() >9? value.getMinutes(): '0'+value.getMinutes())  },
    { field: 'state', headerName: 'Real status', width: 150 },
    { field: 'prediction', headerName: 'Predicted status', width: 150 },
      { field: 'probability', headerName: 'Probability', width: 170, description:'Probability of being buggy',align:'left',
      valueFormatter: ({ value }) => value + '%',},
    ];

    var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    var rows2 = this.props.data.map((i, index) => {
      return {
        id: index,
        commitId: i.id,
        date: i.created_at,
        prediction: i.prediction,
        state:i.state,
        probability: i.probability,
        
      }
    })
  return (
    <div>
    <div style={{ height: 500, width: '100%' }}>
      <DataGrid disableSelectionOnClick={true} rows={rows2} columns={columns} pageSize={7} />
    </div>

{(this.state.commits.length > 0  && this.state.commitId !== 0)&&
  <CommitDialog getNextCommit={this.getNextCommit} commitId={this.state.commitId} projectName={this.state.projectName} gotData={this.state.gotData} commitData={this.state.commit2} data={this.state.commits} show={this.state.showModal} handleData={this.handleData} handleShow={this.handleShow} pointId={this.state.pointId} />}
</div>
  );
}
}
