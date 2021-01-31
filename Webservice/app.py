from flask import Flask,request
from flask_restful import reqparse, abort, Api, Resource
import copy
app = Flask(__name__)
api = Api(app)
Map_user_job = {}
Remote_site_ip=set()
Job_Globle_id=0
miniNode=10
class DynPrvDriver_job(Resource):
    def get(self):
        global Map_user_job

        tmp=copy.deepcopy(Map_user_job)
        Map_user_job={}
        return tmp
    def post(self):
        global Job_Globle_id
        Job_Globle_id += 1
        print(request.form)
        parameter=request.form['parameter']
        jobType = request.form['Job']
        executable = request.form['executable']
        jobInfo={"executable":executable,"Job":jobType,"parameter":parameter,'jobId':Job_Globle_id,"srcLocation":request.form['location']}
        # parameter["Job_id"]=Job_Globle_id
        if request.form["user"] in Map_user_job:
            Map_user_job[request.form["user"]].append(jobInfo)
        else:
            Map_user_job[request.form["user"]]=[jobInfo]
        return Map_user_job[request.form["user"]]

# @app.route('/')
# def hello_world():
#     return 'Hello World!'
#
#
# @app.route("/<string:user_id>")
# def exist_job(user_id):
#     return Map_user_job[user_id] if user_id in Map_user_job else None
class DynPrvDriver_remoteSite(Resource):
    def get(self):
        #global Remote_site_ip
        return Remote_site_ip
    def put(self):
        Remote_site_ip.add(eval(request.form))
        return request.form
class DynPrvDriver_miniNode(Resource):
    def get(self):
        global miniNode
        return str(miniNode)
    def post(self):
        global miniNode
        miniNode=eval(request.form['miniNode'])
        return str(miniNode)
api.add_resource(DynPrvDriver_job,"/job")
api.add_resource(DynPrvDriver_miniNode,"/miniNode")




if __name__ == '__main__':
    app.run(host='0.0.0.0')
